IndoorNav
=========

IndoorNav should demonstrate indoor positioning with image fingerprinting algorithms.

It consists out of 3 parts:

* IndoorNav (Android Application)
    * Takes pictures and displays map, position and current indoor-level
* IndoorNavS (Java Application, Play Framework)
    * Service that accepts images, processes images taken by IndoorNav and tries to determine the position where the image was taken.
* TileGenerator (OpenStreetmap Mapnik with generate_tiles.py)
    * Generates tiles for each indoor-level

Requirements
-----------

* IndoorNav requires "Mock Locations" to work properly. As we publish our position to the LocationProvider of the Android System.
* Tiles with zoomlevel 10 to 20 have to be put in a .zip-File and transfered to your mobile device under /mnt/sdcard/osmdroid/[someTiles].zip
* a
