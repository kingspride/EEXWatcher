# EEXWatcher
This is a very small app to get started with jjoe64's android graphing library.    
It loads electricity market data from my server, and displays it as a simple graph.

## Background
My server is collecting the electricity market data from the official websites and rearranges them to a usable format.    
The app (or any other client) can use the API to retrieve those values.

URL: http://eex.xtlk.de/ (HTTPS not supported)    
Format: `host.tld/ YYYY-MM-DD [ /HH ]`
