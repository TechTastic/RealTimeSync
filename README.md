**Real Time Sync** is a plugin that syncs in-game time to the real time of the server!

Not only that, but the Timezone is configurable per-world! Simply add the following snippet within your world's `config.json` in the `Plugin` section:
```json
"Plugin": {
  "RealTimeSync": {
    "TimeZone": "UTC-6"
  }
}
```

The `Timezone` can be any string parse-able by [`java.time.ZoneId`](https://docs.oracle.com/en/java/javase/23/docs/api/java.base/java/time/ZoneId.html#of(java.lang.String))!

### Potential Future Content
- No Sleeping Skips (technically it will still try but be reset immediately atm)
- Moon Phase Syncing
- Local Weather Syncing