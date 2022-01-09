# Pentagon Bullets 2

## Dev

First terminal:

```
sbt
~server/reStart
```

Second terminal:

```
sbt
~frontend/fastLinkJS
```

Third terminal:

```
cd frontend
npm i  # only once
npx snowpack dev
```

A browser will open at `localhost:8080`. Open a second one to test.

## Build for prod

```
sbt buildApplication
```

Run with

```
java -Dconfig.resource=prod.conf -jar server/target/scala-3.1.0/server-assembly-0.1.0-SNAPSHOT.jar
```

Go to `localhost:9000` in two different browsers...

