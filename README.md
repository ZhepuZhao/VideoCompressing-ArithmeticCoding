# VideoCompressing-ArithmeticCoding
## Introduction
This project is the assignment of COMP 590 Data Compression at UNC-CH.
It aims to use Arithmetic Coding to compress the video file with extension of .dat.

## Structure 
The `src` packages has the following four packages:
- `io`: to read and write any number of bits `Copyright (c)2019 by Ketan Mayer-Patel`
- `ac`: arithmetic encoder and decoder `Copyright (c)2019 by Ketan Mayer-Patel`
- `app`: multiple applications using different schemas with encoder/decoder above to compress and uncompress file
The `data` package contains input and output files.

##
The following table shows the details for each compression schema

| Schema Name  | Compressed File Size(bytes) | Encode Time(ms) | Decode Time(ms)|
| -------------| -------------------------- | --------------- | -------------- | 
|`Static AC Encode (Text Model)`| 1,064,024 | 8242 | 11798 |
|`Adaptive AC Encode (Text Model)`|1,063,224|7095|11067|
|`Context Adaptive AC Encode (Text Model)`|909,144|6262|11894|
|`Average-Neighbor Context Adaptive AC Encode`|894,196|6366|11785|
|(1)`Prior Average-Neighbor Context Adaptive AC Encode`|907,096|6015|11737|
|(2)`Prior Average-Neighbor Context Adaptive AC Encode`|842,856|5755|11581|
|`Multiple Prior Value Context Adaptive AC Encode`|748,376|5333|10553|
|`Prior Value Context Adaptive AC Encode`|670,092|5150|10477|

