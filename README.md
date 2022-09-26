# Transitnet
A back-end of transit network visualization platform supports data query, speed data calculation and so on.
## Getting started
### 1. Database configuration
Ensure that the database is configured correctly in application.properties.
### 2. Data Prepare (Optional)
1. GTFS Data: https://transitfeeds.com/p/mta
2. GTFS RealTime Data: http://bt.mta.info/wiki/Developers/Index

The real-time bus data is preprocessed by data cleaning, stopping point detection and map-matching.

## Notice for database
Online configuration use oceanbase as backend database. On your local machine, you don't have to, and can use a mysql database server instead.
If you really want to use it, put the `lib/oceanbase/oceanbase-client-1.1.5.jar` into `$PROJECT_ROOT$/../lib/oceanbase/` directory so that our project can import it.