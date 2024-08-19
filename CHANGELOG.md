# jsqltranspiler changelog

Changelog of jsqltranspiler.

## 0.6 (2024-06-25)

### Breaking changes

-  JSQLColumnResolver with deeply nested `SelectVisitor` and `FromItemVisitor` ([1693f](https://github.com/starlake-ai/jsqltranspiler/commit/1693f52cc1879e1) Andreas Reichel)

### Features

-  JSQLColumnResolver with deeply nested `SelectVisitor` and `FromItemVisitor` ([1693f](https://github.com/starlake-ai/jsqltranspiler/commit/1693f52cc1879e1) Andreas Reichel)
-  Resolve columns for `WITH ... ` clauses ([fb3dd](https://github.com/starlake-ai/jsqltranspiler/commit/fb3dd73fe95e449) Andreas Reichel)
-  support `EXCEPT` and `REPLACE` clauses ([6bef0](https://github.com/starlake-ai/jsqltranspiler/commit/6bef0de0f235347) Andreas Reichel)
-  add syntax sugar ([dffb6](https://github.com/starlake-ai/jsqltranspiler/commit/dffb63f42644f0e) Andreas Reichel)
-  further Schema Provider and Test simplifications ([23013](https://github.com/starlake-ai/jsqltranspiler/commit/23013fa9f1baa3f) Andreas Reichel)
-  STAR column resolver, wip ([b309d](https://github.com/starlake-ai/jsqltranspiler/commit/b309df83547310d) Andreas Reichel)
-  STAR column resolver, wip ([c20f5](https://github.com/starlake-ai/jsqltranspiler/commit/c20f5add69cc288) Andreas Reichel)
-  STAR column resolver, wip ([c63e1](https://github.com/starlake-ai/jsqltranspiler/commit/c63e10012bd8853) Andreas Reichel)
-  STAR column resolver, wip ([ed49e](https://github.com/starlake-ai/jsqltranspiler/commit/ed49e89c31d13c9) Andreas Reichel)

### Bug Fixes

-  BigQuery default sort order ([2c309](https://github.com/starlake-ai/jsqltranspiler/commit/2c3090e59bba146) Andreas Reichel)
-  BigQuery `SELECT AS STRUCT ...` and `SELECT AS VALUE ...` ([d5352](https://github.com/starlake-ai/jsqltranspiler/commit/d5352cfd8214e8a) Andreas Reichel)
-  BigQuery `GENERATE_DATE_ARRAY` with only 2 parameters ([0264e](https://github.com/starlake-ai/jsqltranspiler/commit/0264ea1ca951b07) Andreas Reichel)

### Other changes

**API URL update**


[67d62](https://github.com/starlake-ai/jsqltranspiler/commit/67d62bdd349b804) Hayssam Saleh *2024-06-12 20:13:03*

**Update readme & licence**


[9e4ea](https://github.com/starlake-ai/jsqltranspiler/commit/9e4eae7161ef4fd) Hayssam Saleh *2024-06-11 11:43:16*


## 0.5 (2024-06-10)

### Features

-  Transpile `EXCEPT` and `REPLACE` clauses ([469af](https://github.com/starlake-ai/jsqltranspiler/commit/469af1cd3973718) Andreas Reichel)
-  Time Key substitutions ([9a269](https://github.com/starlake-ai/jsqltranspiler/commit/9a2692f3433948f) Andreas Reichel)
-  Time Key substitutions ([a093b](https://github.com/starlake-ai/jsqltranspiler/commit/a093b486665503c) Andreas Reichel)

### Other changes

**Update README.md**


[bd8c7](https://github.com/starlake-ai/jsqltranspiler/commit/bd8c7b3cc944570) manticore-projects *2024-06-10 05:26:29*

**Update project root name**

* tests after secret update worked

[527cc](https://github.com/starlake-ai/jsqltranspiler/commit/527cc11b58d1cb3) Hayssam Saleh *2024-06-04 14:00:15*

**test publication by updating secrets**


[3773c](https://github.com/starlake-ai/jsqltranspiler/commit/3773c338facfe42) Hayssam Saleh *2024-06-04 13:57:36*

**Sonatype credentials passed through gradle.properties**


[48dff](https://github.com/starlake-ai/jsqltranspiler/commit/48dffc3215367f4) Hayssam Saleh *2024-06-04 06:53:35*

**Do not sign snapshots**


[1e052](https://github.com/starlake-ai/jsqltranspiler/commit/1e0524dd90b521f) Hayssam Saleh *2024-06-04 06:52:09*


## 0.4 (2024-06-04)

### Features

-  support Insert, Update, Delete and Merge statements ([bb1be](https://github.com/starlake-ai/jsqltranspiler/commit/bb1be258dbc1f4f) Andreas Reichel)
-  support Insert, Update, Delete and Merge statements ([58dd8](https://github.com/starlake-ai/jsqltranspiler/commit/58dd86fb296de24) Andreas Reichel)
-  INSERT, UPDATE, DELETE, MERGE transpilers ([e53e9](https://github.com/starlake-ai/jsqltranspiler/commit/e53e9a0239440b0) Andreas Reichel)
-  Databricks Aggregate functions ([a4491](https://github.com/starlake-ai/jsqltranspiler/commit/a4491aa0079c0ba) Andreas Reichel)
-  Databricks Aggregate functions ([80872](https://github.com/starlake-ai/jsqltranspiler/commit/8087289120ae747) Andreas Reichel)
-  Databricks Aggregate functions ([f5653](https://github.com/starlake-ai/jsqltranspiler/commit/f5653c86d5ac5fa) Andreas Reichel)
-  Databricks Aggregate functions ([662fa](https://github.com/starlake-ai/jsqltranspiler/commit/662fa5a83953b5f) Andreas Reichel)

### Other changes

**improve mock**


[b23f2](https://github.com/starlake-ai/jsqltranspiler/commit/b23f25c6f6d78d4) Hayssam Saleh *2024-05-29 13:50:39*

**Proposed interface & mock implementation for tests case**


[33292](https://github.com/starlake-ai/jsqltranspiler/commit/33292e81ca0e5a2) Hayssam Saleh *2024-05-29 13:40:25*


## 0.2 (2024-05-27)

### Features

-  Quote DuckDB keywords in Table, Column and Alias ([7ce96](https://github.com/starlake-ai/jsqltranspiler/commit/7ce96b21a2bbd78) Andreas Reichel)
-  provide methods accepting prepared `ExecutorService` and `Consumer` ([4cd03](https://github.com/starlake-ai/jsqltranspiler/commit/4cd03f665a66da2) Andreas Reichel)
-  provide methods accepting prepared `ExecutorService` and `Consumer` ([480c8](https://github.com/starlake-ai/jsqltranspiler/commit/480c82da7dcad26) Andreas Reichel)
-  Databricks Date functions ([8e486](https://github.com/starlake-ai/jsqltranspiler/commit/8e486c7f346d8d8) Andreas Reichel)
-  get the Macros as text collection or array ([a2ece](https://github.com/starlake-ai/jsqltranspiler/commit/a2ecea72f4da4bc) Andreas Reichel)
-  Snowflake math functions, complete ([4cb7e](https://github.com/starlake-ai/jsqltranspiler/commit/4cb7e85007d54a6) Andreas Reichel)
-  Add missing Redshift conversion functions ([649ed](https://github.com/starlake-ai/jsqltranspiler/commit/649edd4fe1d3c0e) Andreas Reichel)
-  Snowflake conversion functions ([e63ac](https://github.com/starlake-ai/jsqltranspiler/commit/e63ac784f0d3a3d) Andreas Reichel)
-  Snowflake array functions ([61522](https://github.com/starlake-ai/jsqltranspiler/commit/615228255d135bb) Andreas Reichel)
-  Snowflake aggregate function ([f7a20](https://github.com/starlake-ai/jsqltranspiler/commit/f7a20930f061029) Andreas Reichel)
-  Snowflake TEXT functions complete ([b5875](https://github.com/starlake-ai/jsqltranspiler/commit/b5875b0a2d547dd) Andreas Reichel)
-  rework UnitTest and support Prologues and Epilogues as per test ([79997](https://github.com/starlake-ai/jsqltranspiler/commit/799975daf13b0c4) Andreas Reichel)
-  Snowflake DateTime function and Structs with virtual columns ([800a6](https://github.com/starlake-ai/jsqltranspiler/commit/800a6e971ea8f67) Andreas Reichel)
-  Snowflake DateTime functions ([cfa5e](https://github.com/starlake-ai/jsqltranspiler/commit/cfa5e36a62bfd4e) Andreas Reichel)
-  fascilitate BigQuery and Snowflake and add SQLGlot Tests for all ([cdd10](https://github.com/starlake-ai/jsqltranspiler/commit/cdd10dd7192c2e9) Andreas Reichel)
-  RedShift Window Functions complete ([a84e7](https://github.com/starlake-ai/jsqltranspiler/commit/a84e7e2a0e83214) Andreas Reichel)
-  RedShift Window functions ([b50db](https://github.com/starlake-ai/jsqltranspiler/commit/b50dbef060d60cc) Andreas Reichel)
-  RedShift Aggregate functions ([7cd27](https://github.com/starlake-ai/jsqltranspiler/commit/7cd2734d4cce83b) Andreas Reichel)
-  Redshift MATH functions ([641ab](https://github.com/starlake-ai/jsqltranspiler/commit/641ab58974382a5) Andreas Reichel)
-  Redshift ARRAY functions ([cfc06](https://github.com/starlake-ai/jsqltranspiler/commit/cfc069ca425a250) Andreas Reichel)
-  Redshift DateTime functions completed ([832eb](https://github.com/starlake-ai/jsqltranspiler/commit/832eb1874209119) Andreas Reichel)
-  Redshift DateTime functions ([87bc4](https://github.com/starlake-ai/jsqltranspiler/commit/87bc4d746f03338) Andreas Reichel)
-  auto-cast ISO_8601 DateTime Literals ([d225b](https://github.com/starlake-ai/jsqltranspiler/commit/d225b95799bc119) Andreas Reichel)
-  Redshift DateTime functions, wip ([ac574](https://github.com/starlake-ai/jsqltranspiler/commit/ac574e32f93ca02) Andreas Reichel)
-  complete Redshift TEXT functions ([9acff](https://github.com/starlake-ai/jsqltranspiler/commit/9acffbae8be1ce9) Andreas Reichel)
-  Redshift String functions ([2154d](https://github.com/starlake-ai/jsqltranspiler/commit/2154d02118d1ec5) Andreas Reichel)
-  redshift string functions ([7544f](https://github.com/starlake-ai/jsqltranspiler/commit/7544f9d34aa69d2) Andreas Reichel)
-  Adopt Implicit Cast and better Type information ([4be22](https://github.com/starlake-ai/jsqltranspiler/commit/4be2257be97eacc) Andreas Reichel)

### Bug Fixes

-  complete DataBricks text functions ([66a37](https://github.com/starlake-ai/jsqltranspiler/commit/66a3720f54eb669) Andreas Reichel)
-  DataBricks text functions ([9f312](https://github.com/starlake-ai/jsqltranspiler/commit/9f312caa3ad48e0) Andreas Reichel)
-  DataBricks text functions ([c9361](https://github.com/starlake-ai/jsqltranspiler/commit/c93613dd5e1ef61) Andreas Reichel)
-  ByteString handling ([a9da4](https://github.com/starlake-ai/jsqltranspiler/commit/a9da45b4e27c4d0) Andreas Reichel)
-  Stack-overflow when RedShift Expression Transpiler calling SUPER ([55276](https://github.com/starlake-ai/jsqltranspiler/commit/55276788695cc02) Andreas Reichel)

### Other changes

**Add snapshot Github Action**


[f64f5](https://github.com/starlake-ai/jsqltranspiler/commit/f64f5f8c37f6a6b) Hayssam Saleh *2024-05-20 15:11:05*

**Fix artifact group name**


[a7cde](https://github.com/starlake-ai/jsqltranspiler/commit/a7cde0d1158d375) Hayssam Saleh *2024-04-16 12:54:48*

**This commit to fix the final package names and keep Andreas Reichel as the only developer of this initial version.**


[236b4](https://github.com/starlake-ai/jsqltranspiler/commit/236b42af06b8e6f) Hayssam Saleh *2024-04-04 18:20:47*


## 0.1 (2024-04-04)

### Features

-  Complete the Aggregate functions ([732d9](https://github.com/starlake-ai/jsqltranspiler/commit/732d9ec4533be86) Andreas Reichel)
-  Array functions ([d13d7](https://github.com/starlake-ai/jsqltranspiler/commit/d13d7a25cc5e08f) Andreas Reichel)
-  more Aggregate functions ([0c32a](https://github.com/starlake-ai/jsqltranspiler/commit/0c32a055fb819fb) Andreas Reichel)
-  more Aggregate functions ([57d74](https://github.com/starlake-ai/jsqltranspiler/commit/57d74642ceab60b) Andreas Reichel)
-  Aggregate Functions, wip ([ad7d9](https://github.com/starlake-ai/jsqltranspiler/commit/ad7d932417fa7aa) Andreas Reichel)
-  complete the BigQuery Math functions ([25a25](https://github.com/starlake-ai/jsqltranspiler/commit/25a2506037a4fb7) Andreas Reichel)
-  add MATH functions ([20ffa](https://github.com/starlake-ai/jsqltranspiler/commit/20ffa7756e00c17) Andreas Reichel)
-  completed the TEXT functions ([a5a28](https://github.com/starlake-ai/jsqltranspiler/commit/a5a2835c5cf291d) Andreas Reichel)
-  more String functions incl. Lambda based transpilation ([38f30](https://github.com/starlake-ai/jsqltranspiler/commit/38f30a014b7de29) Andreas Reichel)
-  support BigQuery Structs, DuckDB structs and translation ([12aed](https://github.com/starlake-ai/jsqltranspiler/commit/12aed9ad29a10c2) Andreas Reichel)
-  support more BigQuery Date/Time functions ([0599f](https://github.com/starlake-ai/jsqltranspiler/commit/0599f3811515dcf) Andreas Reichel)
-  support more BigQuery Date/Time functions ([3dd69](https://github.com/starlake-ai/jsqltranspiler/commit/3dd691821011f33) Andreas Reichel)
-  implement a Python SQLGlot based test for comparision ([7ac34](https://github.com/starlake-ai/jsqltranspiler/commit/7ac3428e2c51e32) Andreas Reichel)
-  support more BigQuery Date/Time functions ([34ad3](https://github.com/starlake-ai/jsqltranspiler/commit/34ad3f8df0ce36a) Andreas Reichel)
-  many more DateTime functions ([8769d](https://github.com/starlake-ai/jsqltranspiler/commit/8769dc8fec7a2d1) Andreas Reichel)
-  many more Google BigData date functions ([cd40e](https://github.com/starlake-ai/jsqltranspiler/commit/cd40edabbb1a874) Andreas Reichel)
-  date parts ([cf94d](https://github.com/starlake-ai/jsqltranspiler/commit/cf94d5b1f14898a) Andreas Reichel)
-  `DATE_DIFF()` function ([a497d](https://github.com/starlake-ai/jsqltranspiler/commit/a497d80293713f6) Andreas Reichel)
-  CLI ([6c836](https://github.com/starlake-ai/jsqltranspiler/commit/6c8360ae3f73f05) Andreas Reichel)
-  functions ([6f6b1](https://github.com/starlake-ai/jsqltranspiler/commit/6f6b127fb37f3b0) Andreas Reichel)
-  functions ([44cf6](https://github.com/starlake-ai/jsqltranspiler/commit/44cf635ef2ae996) Andreas Reichel)
-  `TOP ...` rewrite ([567ac](https://github.com/starlake-ai/jsqltranspiler/commit/567acd96ca5a306) Andreas Reichel)

### Other changes

**progress the functional mapping**

* Signed-off-by: Andreas Reichel &lt;andreas@manticore-projects.com&gt;

[3db6b](https://github.com/starlake-ai/jsqltranspiler/commit/3db6beacd448544) Andreas Reichel *2024-03-14 03:23:50*

**Initial commit**


[80659](https://github.com/starlake-ai/jsqltranspiler/commit/80659ff37162a24) manticore-projects *2024-03-13 07:22:23*
