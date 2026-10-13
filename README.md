# MyWeather

An Android app that answers a simple question: *given the weather for the next 7 days, what's
worth doing in this city?* You search for a city, pick it, and for each day you get Skiing,
Surfing, Outdoor sightseeing and Indoor sightseeing ranked from best to worst, each with a score
from 0 to 100 and a short reason.

Data comes from the [Open-Meteo](https://open-meteo.com/) Geocoding and Forecast APIs. There is no
backend and no API key.

The brief said architecture, correctness and testing matter more than looks, so that's where I
spent the time. The UI is plain Material 3.

---

## Platform and tooling

| | |
|---|---|
| Language | Kotlin 2.2 |
| UI | Jetpack Compose + Material 3 (Compose BOM 2026.02.01) |
| Min / target SDK | 26 / 37 |
| Build | Gradle 9.6, AGP 9.4.1, Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |
| DI | Hilt |
| Networking | Retrofit 3 + OkHttp 5 + kotlinx.serialization |
| Async | Coroutines + Flow |
| Persistence | DataStore Preferences (only for the theme setting) |
| Tests | JUnit 4, Turbine, kotlinx-coroutines-test, MockWebServer, Compose UI test on Robolectric, Compose Preview Screenshot Testing |

I picked Android because it's the platform I work on day to day. Every library here is the
standard choice for its job, so a reviewer shouldn't need to learn anything new to read the code.

## Architecture

Single `app` module, packaged by layer. Dependencies only point inward:
`presentation → domain ← data`.

```
di/                  Hilt modules (network, repositories, dispatchers, DataStore)
data/remote/         Retrofit APIs, DTOs, DTO → domain mappers
data/repository/     Repository implementations, safeCall error mapping
domain/model/        City, DailyForecast, Activity, ActivityScore, DayRanking, DomainError...
domain/repository/   Repository interfaces
domain/scoring/      Pure scoring functions, one per activity, plus the ranker
domain/usecase/      SearchCitiesUseCase, GetActivityRankingUseCase
presentation/        Screens, ViewModels, UiState / Event types, navigation, theme
```

Decisions worth calling out:

- **The domain is plain Kotlin.** No Android imports; the only annotation is `@Inject`. The
  scorers have no I/O and no clock, so the same forecast always gives the same ranking. That's what
  makes them easy to test exhaustively.
- **DTOs never leave `data`.** Open-Meteo returns parallel arrays (`time[]`, `temperature_2m_max[]`,
  ...) whose values can be `null` and whose lengths aren't guaranteed to match. The mapper turns
  them into one `DailyForecast` per day, with nullable fields instead of crashes.
- **Repositories return `Result` and never throw at the UI.** `safeCall` runs the request on an
  injected dispatcher and maps every failure to a `DomainError` (`Network`, `Timeout`, `Server`,
  `InvalidResponse`, `Unknown`). Cancellation is always rethrown, never swallowed.
- **State is a sealed type, never a bag of booleans.** Each ViewModel exposes one `StateFlow` of a
  sealed `UiState`. For example, search is `Idle | Loading | Success | Empty | Error`, and each
  state carries the current query text. One-off things (navigate, show a snackbar) go through a
  `Channel` as events, so they aren't replayed after rotation.
- **Composables are stateless.** Screens get state plus callbacks, so the same composable is used
  by the app, the previews and the screenshot tests.
- **Reasons are keys, not strings.** Scorers return a `ReasonKey` enum. The presentation layer maps
  it to `strings.xml`, which keeps the domain free of Android resources and the text translatable.

## Build and run

You need a recent Android Studio that supports AGP 9.4 and an emulator or device on API 26+.

1. Clone the repo and open it in Android Studio.
2. Run the `app` configuration.

From the command line (`JAVA_HOME` must point to a JDK; the one bundled with Android Studio works):

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # build and install on a connected device/emulator
```

The Gradle daemon is set to JDK 25 (`gradle/gradle-daemon-jvm.properties`); the JDK bundled with
Android Studio satisfies it.

## Tests

```bash
./gradlew testDebugUnitTest            # 184 tests on the JVM, Compose UI tests included
./gradlew validateDebugScreenshotTest  # 26 screenshot references
./gradlew lintDebug
```

To regenerate screenshot references after an intentional UI change:
`./gradlew updateDebugScreenshotTest`.

### Strategy

I tested the parts that decide what the user sees, and kept the tests independent of Android
wherever possible.

| Area | Tests | What they pin down |
|---|---|---|
| Scoring (`domain/scoring`) | 72 | Ideal and bad days per activity, exact threshold edges, missing data, tie-breaks, determinism |
| Use cases | 14 | Blank queries skip the network, errors pass through untouched, every day gets all 4 activities |
| Data layer | 36 | Mappers with nulls / short arrays / bad dates; both repositories against MockWebServer: success, 4xx, 5xx, 429, malformed JSON, wrong types, timeout, dropped connection, cancellation |
| ViewModels | 35 | State transitions with Turbine on a `StandardTestDispatcher`: search debounce, stale results being dropped, retry, refresh keeping old data visible, theme save failures |
| Small presentation helpers | 5 | Error → message mapping, city display name |
| Compose UI (Robolectric) | 22 | Each screen with a given state: what's shown, and that taps, typing, back and pull-to-refresh call the right callback; snackbars for a failed theme save and a failed refresh |
| Screenshots | 13 previews × light/dark | Every state of the Search, Ranking and Settings screens, plus the bottom bar |

Test names describe behaviour, for example
`given rain, when ranking, then indoor beats outdoor`. I used hand-written fakes instead of mocks:
they're shorter, and they fail in a more readable way.

The UI tests run on Robolectric, so the whole suite runs with one command and no emulator. Because
the screens are stateless, each test just builds a `UiState`, renders it and checks the callbacks;
there's no Hilt or network involved.

**What isn't covered:** navigation end to end (search → ranking → back) with the real ViewModels
and Hilt. Each piece is tested on its own, but not the full journey. I checked that by hand on an
emulator.

## API usage

**Geocoding:** `GET https://geocoding-api.open-meteo.com/v1/search?name={query}&count=10`
- When nothing matches, Open-Meteo leaves out the `results` field entirely. That is treated as an
  empty list, not an error.
- Results without a name or coordinates are dropped, since they can't be forecast.

**Forecast:** `GET https://api.open-meteo.com/v1/forecast`
- `latitude`, `longitude` from the selected city
- `daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,snowfall_sum,wind_speed_10m_max,wind_gusts_10m_max`
- `forecast_days=7` (today plus the next 6 days)
- `timezone=auto`, so the days line up with the city's local calendar and not the phone's
- Default units: °C, mm, cm for snowfall, km/h for wind

**Errors:**

| What happened | Domain error | What the user sees |
|---|---|---|
| No connection / connection dropped | `Network` | "Can't reach the server. Check your connection." |
| No answer within 15 s | `Timeout` | "The request took too long..." |
| HTTP 5xx or 429 | `Server(code)` | "The weather service is unavailable right now..." |
| Other 4xx, malformed JSON, wrong types, no `daily` block | `InvalidResponse` | "The server sent a response we couldn't read." |
| Anything else | `Unknown` | "Something went wrong." |

The JSON parser ignores unknown keys, so new fields added by Open-Meteo won't break the app.

## How activities are ranked

Each activity has a scorer. A scorer takes one day's forecast, turns each weather factor into a
0–1 fraction using named thresholds, and computes a weighted average from 0 to 100. Weights add up
to 100 per activity, and every threshold is a documented constant in the scorer's file.

| Activity | Factors (weight) | Main rules |
|---|---|---|
| **Skiing** | Snowfall 40, temperature 30, wind 20, gusts 10 | No fresh snowfall means the score is **0**. Full snow points from 5 cm. Daily max ≤ 0 °C keeps snow good; ≥ 5 °C is "too warm". |
| **Surfing** | Wind 35, gusts 15, precipitation 20, sky 20, temperature 10 | Wind is a band: ≤ 5 km/h is flat, 15–30 is ideal, ≥ 50 is blown out. Gusts ≥ 70 km/h and ≥ 10 mm of rain count as bad. |
| **Outdoor sightseeing** | Sky 30, precipitation 25, temperature 25, wind 10, gusts 10 | Most comfortable between 15 and 28 °C max; temperature points reach zero at 0 and 38 °C. Just outside the band the day is "cool" or "warm", and only from 7.5 / 33 °C (half the temperature points gone) is it "too cold" / "too hot". At ≥ 8 mm of rain, wind ≥ 55 km/h or gusts ≥ 80 km/h, that factor scores zero and becomes the reason. |
| **Indoor sightseeing** | The same factors as outdoor, inverted (rain 40, sky 35, temperature 15, wind 5, gusts 5) | The worse it is outside, the better indoors looks. Never below 30, because a museum is always an option. |

"Sky" comes from the WMO `weather_code`: clear = 1.0, partly cloudy = 0.8, overcast = 0.6,
fog = 0.4, drizzle = 0.3, rain or snow = 0.1, thunderstorm = 0. Unknown codes count as 0.5.

**The reason** shown under each score is the first rule that matches, checked from most to least
severe. For outdoor sightseeing that's storm, rain, strong wind, then too cold or too hot. Each
activity also has its own reasons ("No snow for the day", "Too calm, likely flat", "Windy, surf may
be choppy", "Warm, find some shade"...). Where a factor is scored on a band, the reason follows the
same band, so a label like "too hot" only appears once that factor has really pulled the score down.

**Missing data:** if a value is `null`, that factor is skipped and the other weights are
rescaled, and the UI lists it as "Not considered (no data)". If everything is missing, the score is
a neutral 50.

**Ties** are broken by a fixed order (Skiing, Surfing, Outdoor, Indoor), so the list never
reshuffles between two identical requests.

## Assumptions

- **Weather fields.** I used daily aggregates because the app ranks whole days. The seven fields
  above cover what actually decides these activities: sky, rain, snow, temperature, wind, gusts. I
  used the daily **max** temperature because it describes the part of the day people are out.
- **Skiing needs fresh snow that day.** The Forecast API has no daily snow depth, so a resort with
  a good base but no new snowfall scores 0. I chose that on purpose: it keeps the logic simple and
  never suggests skiing somewhere with no snow. The forecast is also for the city's coordinates, not
  the slopes above it.
- **Surfing uses wind as a proxy for waves.** The brief points to the Geocoding and Forecast APIs
  only, and the Forecast API has no wave data. Some wind builds swell and too much blows it out, so
  wind gets a band and the most weight. Wind direction isn't used.
- **"Next 7 days" includes today**, in the city's timezone.
- **The ranking is per day.** I read "a ranked list of activities over the next 7 days" as one
  ranking for each day, because "is tomorrow good for surfing?" is the more useful question.

## Trade-offs and omissions

- **Inland surfing.** Because of the proxy above, a windy, warm day in Zurich can rank surfing
  high. Neither API says whether a place is on the coast. Open-Meteo's Marine API (wave height) would
  fix both problems; I left it out to stay within the two APIs in the brief.
- **Reason vs. score.** The reason names the main limiting factor, while the score is a weighted
  average. A calm but sunny day can show "Too calm, likely flat" with a surfing score around 80. I
  kept it like this because the reason explains what holds the score back, but a "verdict" style
  reason would read more naturally.
- **No weekly summary.** There's no "best activity this week" at the top; you scan the days.
- **No offline cache.** Without a connection you get an error and a retry button. Room plus a
  "last updated" label would be next.
- **Splash background.** It follows the system light/dark setting, not the theme picked in the
  app, because Android draws it before the app can read the saved preference.
- **Extras beyond the brief:** pull-to-refresh on the ranking (old data stays visible while
  refreshing), a light/dark/system theme setting saved in DataStore, a splash screen held until the
  theme is known, and screenshot tests.

## Production readiness

Ready:
- Clear error handling, with retry on both screens and debounced search with stale results dropped.
- R8 is enabled for release builds. `assembleRelease` builds, but I haven't run the minified APK.
- Coroutine cancellation is respected throughout, and dispatchers are injected.
- Strings live in `strings.xml`, so translation is just resources.

Before shipping I would add:
- **Open-Meteo terms.** The free API is for non-commercial use. A real product needs their paid
  plan (or another provider) and an API key handled outside the repo.
- **Caching** of forecasts, both for offline use and to stay well under rate limits.
- **An end-to-end navigation test** with Hilt test bindings, and CI running the whole suite on
  every PR.
- **Crash reporting and analytics** (which cities, which activities, error rates).
- **An accessibility pass** with TalkBack and large fonts. Scores are plain text, but I haven't
  audited it.
- **Release signing and a CI pipeline.** None are set up.
- **Localization** (only English today) and locale-aware units (°F, mph).
- **Tuning the thresholds** against real feedback. They are reasonable guesses, not research.

## Cross-platform notes

If this needed an iOS version, I'd share the logic rather than rewrite it:

- `domain/` (models, scorers, ranker, use cases) is pure Kotlin and is the obvious candidate for a
  **Kotlin Multiplatform** shared module. The main change is replacing `java.time.LocalDate` with
  `kotlinx-datetime`. The `data` layer would move from Retrofit to Ktor, keeping the same repository
  interfaces.
- The UI would stay native: SwiftUI on iOS, observing the shared ViewModels or a thin wrapper per
  screen. The sealed UiState types map naturally to Swift enums.
- If sharing code isn't an option, the scoring tests work as a spec: same inputs, same expected
  scores. Porting them to XCTest first would keep both apps ranking identically.

## AI usage

I used AI coding assistants (Claude Code) during development, mainly to speed up boilerplate,
generate test cases around the edges I defined, review the project against the brief, and help draft
this README. The architecture, the scoring rules and thresholds, and the trade-offs above are my
decisions.

How I verified things:
- Changes went through short-lived branches and PRs, which I reviewed before merging.
- The unit and screenshot suites, plus lint, had to pass before merging.
- The fixes after the review against the brief (surf wind reason, server errors, search retry,
  theme save failure) each started with a failing test that passed after the change.
- I ran the app on an emulator against the live API: searching "Lisbon", opening the ranking, and
  checking that each of the 7 days lists all four activities with a score and a reason. That's how
  I noticed the reason vs. score behaviour described in the trade-offs.
