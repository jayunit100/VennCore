1) ODB errors include 

file locks 

and 

empty file error


To prevent a file lock, make sure every access has a "close" after it.

To prevent a file empty error, never create a file .... just reference a new file name
and odb will automatically make it for you. 

