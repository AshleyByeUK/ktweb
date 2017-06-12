# KtWeb

## About

This is an implementation of [https://github.com/denisftw/modern-web-kotlin](https://github.com/denisftw/modern-web-kotlin),
based on the book [https://leanpub.com/modern-web-development-with-kotlin](https://leanpub.com/modern-web-development-with-kotlin).

## Instructions

Obtain an [Open Weather Map App ID](https://openweathermap.org/appid) and paste the value
into *development.json* and *production.json*.

To install npm dependencies:

    npm i

To watch for changes to assets:

    npm run watch
    
To run in development:

    pg_ctl -D /usr/local/var/postgres start
    ./gradlew run

To package for production:

    npm run production
    
Then build uber-JAR:

    ./gradlew shadowjar

Make a directory beneath project root to simulate production environment and copy
the necessary files and directories:

    mkdir -p ../ktweb-production/logs
    cp -r {./build/libs/ktweb-1.0-SNAPSHOT-all.jar,./public,./conf} ../ktweb-production
    cd ../ktweb-production

Ensure postgresql is running:

    pg_ctl -D /usr/local/var/postgres start

Launch the application:

    java -cp ./ktweb-1.0-SNAPSHOT-all.jar io.vertx.core.Launcher run uk.ashleybye.verticles.MainVerticle -conf conf/production.json

Provided all has gone as expected, the application is available at
[http://localhost:9000/home](http://localhost:9000/home),
[http://localhost:9000/login](http://localhost:9000/login), and
[http://localhost:9000/hidden/admin](http://localhost:9000/hidden/admin). For
logging in, username is *ktest*, password is *password123*.

To stop the server and postgresql, issue the following commands respectively:

    Ctrl-C
    pg_ctl -D /usr/local/var/postgres stop