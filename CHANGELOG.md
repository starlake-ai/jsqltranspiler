# jsqltranspiler changelog

Changelog of jsqltranspiler

## 1.17

Released 2026-11-18.

### Dependencies

* **jsqlparser 5.4.272** (was 5.4.162 in 1.16).
* Tested against **DuckDB 2.0** (`duckdb_jdbc` 2.0.0.0, was 1.5.2.0). The
  transpiled SQL now needs DuckDB 1.3 or later, for the `lambda` syntax.

### Fixed

* Column resolver: expanding `*` or `t.*` over a catalog column whose name
  holds a dot, such as `"a.b"`, no longer produces a reference to another
  table. The column used to come out as `a.b` (column `b` of a table `a`),
  which fails to bind or silently reads another table's column. It now comes
  out quoted and qualified by its own table, `dotted."a.b"` (#156).
* Date units are printed upper case whatever the jsqlparser version. Since
  5.4.272 jsqlparser keeps a date unit as written (`hour`), so the transpiled
  literals changed from `DATEDIFF('HOUR', ...)` to `DATEDIFF('hour', ...)`.
* DuckDB 2.0 compatibility:
  * Lambdas are written as `lambda x : ...`; DuckDB 2.0 rejects the arrow form
    `x -> ...` by default.
  * `createMacros` sets `geometry_always_xy = false` (globally, when the
    spatial extension provides it), which DuckDB 2.0 requires before running
    axis order sensitive functions such as `ST_Distance_Sphere`.
  * Snowflake `DIV0` and `DIV0NULL` become `CASE WHEN b = 0 THEN 0 ELSE a / b
    END`. The previous `COALESCE(DIVIDE(a, b), 0)` raises an error on DuckDB 2.0
    and was an integer division: `DIV0(1, 2)` returned 0 instead of 0.5.
  * BigQuery `UNIX_MILLIS` becomes `CAST(FDIV(EPOCH_US(ts), 1000) AS BIGINT)`
    and rounds down, as BigQuery does. `EPOCH_MS` rounds on DuckDB 2.0 and
    truncated towards zero on 1.x, which was wrong before 1970.
  * BigQuery `ST_GEOGFROMTEXT` keeps the WKT argument only, with a warning.
    DuckDB 2.0 rejects the named `oriented => ...` argument, and DuckDB 1.x had
    bound it to its unrelated `ignore_invalid` parameter.

### Build

* JaCoCo coverage gate raised to 2700 missed lines (was 2600, which `main`
  already exceeded).

## 1.16

Released 2026-10-21.

### Dependencies

* **jsqlparser 5.4.162**, unchanged from 1.15.

### Fixed

* Column lineage no longer fails with a `NullPointerException` on expressions
  with absent optional parts, such as `trim(col)`, `col LIKE 'x%'` (no
  `ESCAPE`) or `* LIKE 'col%'`. jsqlparser 5.4.16x passes those absent parts to
  the expression visitor as `null`; `JSQLExpressionColumnResolver` now skips
  them, and the lineage of the present parts is unchanged.

## 1.15

Released 2026-10-19.

### Dependencies

* **jsqlparser 5.4.162**, unchanged from 1.14.

### Fixed

* Column lineage no longer fails with a `NullPointerException` when a query
  names a catalog or schema that the metadata does not contain, for example
  `acme_tpch.tpch1.orders` when only `tpch1.orders` is known. In `LENIENT` and
  `IGNORE` modes, `JdbcMetaData.getColumn` now returns `null` for an unknown
  catalog or schema, as it already did for an unknown table, so the column is
  reported as unresolved. `STRICT` mode still throws.

## 1.14

Released 2026-10-18.

### Dependencies

* **jsqlparser 5.4.162** (was 5.4.15 in 1.13). jsqlparser 5.4 replaces the
  `EXCEPT`/`REPLACE` lists on `AllColumns` with a list of `ColumnsTransformer`s.
  The full test suite (1065 tests) is green against 5.4.162.

### Changed

* Migrate `*` and `t.*` handling to the `ColumnsTransformer` model:
  * The transpiler rewrites `EXCEPT` to DuckDB's `EXCLUDE`, and passes
    `EXCLUDE`, `REPLACE` and `RENAME` through unchanged.
  * It drops ClickHouse-only constructs that DuckDB cannot express, each with a
    warning: `EXCEPT 'pattern'`, `APPLY(...)` and the `STRICT` modifier.
  * The column resolver applies `EXCEPT`, `REPLACE` and `RENAME` when it expands
    star columns, and matches ClickHouse `EXCEPT 'pattern'` on a best-effort
    basis with a Java regex.

### Build

* Release builds no longer register `mavenLocal()` or the central snapshots
  repository, so the jsqlparser range resolves only to Maven Central releases.
  POM generation now fails a release that would still pin a snapshot
  dependency. The first 1.14 upload pinned `jsqlparser:5.5.173-SNAPSHOT` and
  Maven Central rejected it, so this is the build that ships as 1.14.
* Make `publish.sh` executable.

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
