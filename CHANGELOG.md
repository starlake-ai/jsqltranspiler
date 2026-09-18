# jsqltranspiler changelog

Changelog of jsqltranspiler

## 1.13

Released 2026-09-18.

### Dependencies

* **jsqlparser 5.4.15** (was 5.4.2 in 1.12). Notable upstream changes: BigQuery
  `JSON 'literal'` string literals parse again (upstreamed from the starlake-ai
  fork, JSQLParser#2488), plus grammar fixes for `MERGE ... WHEN NOT MATCHED BY
  TARGET / BY SOURCE`, `XMLTABLE`, PostgreSQL `GROUPS` window frames, structured
  interval qualifiers and nested parametric `CAST` targets. The full test suite
  (1050 tests) is green against 5.4.15.

### Fixed

* Resolve correlated sub queries against the enclosing query (#152).
* Override the `UnPivotQuery` visit methods in `JSQLColumResolver`: jsqlparser
  5.4.15 adds `UnPivotQuery` with default `visit()` in both `SelectVisitor` and
  `FromItemVisitor`, which broke compilation (same pattern as `PivotQuery` in
  1.12).

## 1.12

Released 2026-09-14.

### Dependencies

* **jsqlparser 5.4.2** (was 5.3.336 in 1.11). The release POM pins the newest
  `com.manticore-projects.jsqlformatter:jsqlparser` release on Maven Central.
  Development builds continue to track the newest manticore snapshot. The full
  test suite (1050 tests) passes against both lines, so no source change was
  required for the upgrade.

### Documentation

* Align `PUBLISHING.md` with the actual `publish.sh` flow.

### Build

* Ignore `.bsp/`, `.claude/` and the `tickitdb.zip` fixture that the test suite
  downloads on demand.
