# production-and-consumption_analytics

A production & consumption data visualizer app.

TODO:
- Receive and accumulate data to user account from sensors.
- Add/Remove units of measurement.
- Visualize data

## Development Mode

### Run application:

```
lein clean
lein figwheel dev
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```
lein clean
lein cljsbuild once min
```
