# Privacy

Torrents is a BitTorrent client, so the one thing worth knowing is built into what it does:
it trades pieces of files directly with strangers' phones and computers.

## What leaves the phone

- **Your internet address, to the other people in each torrent.** That is how BitTorrent
  works. Everyone downloading or sharing the same torrent can see it while you are in it.
- **What you are downloading, to the trackers named in each torrent**, and to the public
  DHT network. The app joins the DHT by way of `dht.libtorrent.org`,
  `router.bittorrent.com`, `dht.transmissionbt.com` and `outer.silotis.us`.
- **Requests for the links and RSS feeds you give it.**

There is nothing else. No analytics, no telemetry, no advertising identifier, no update
check, and no account.

If you want your address hidden from the other people in a torrent, put the phone on a VPN
or set a proxy under *Settings → Network*, and turn on *Require proxy for all connections*.

## Crash reports

When the app crashes it offers to report it. Nothing is sent unless you press *Report*,
which opens your own mail app with the report addressed to `email@wanderthe.dev`, where you
can read it, change it or throw it away before it goes.

## Permissions

`app/src/main/AndroidManifest.xml` asks for:

| permission | why |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE` | to download and share, and to keep to Wi-Fi or unmetered connections when you ask it to |
| `WAKE_LOCK`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS` | to keep a download going with the screen off, and to say so in a notification |
| `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE` | to save files where you choose and to open `.torrent` files |
| `RECEIVE_BOOT_COMPLETED`, `QUICKBOOT_POWERON`, `SCHEDULE_EXACT_ALARM` | for the optional start-on-boot and the download scheduler |
| `VIBRATE` | for the optional buzz when a download finishes |
| `INSTALL_SHORTCUT` | for the "Add torrent" home-screen shortcut |

AndroidX also adds `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` to every app it builds. It lets
nothing in or out; it only keeps the app's own internal messages private to it.

## What is stored

Your torrents, their files, your feeds and your settings, on the phone and nowhere else.

## Checking for yourself

```
aapt2 dump badging honryu.apk | grep uses-permission
```
