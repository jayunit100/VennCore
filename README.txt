This is the core of venn.   

It can be used :

- To rebuild Venn :) 

- To build mashups similar to Venn which use EBI / NCBI web services

- To learn how to use Neodatis in a real application that is performant.

- As a sandbox for datamining against the PDB.  It is capable of reasonably fast protein-protein lookups and comparisons, since 
it stores native PDB java objects and deserializes them directly from a byte stream.  

- There is an adoption of the older biojava Needleman-Wunsch implementation for amino acids which uses 2D arrays.  


---- A note on ODB errors ----

file locks 

and 

empty file error


To prevent a file lock, make sure every access has a "close" after it.

To prevent a file empty error, never create a file .... just reference a new file name
and odb will automatically make it for you. 

