# EulerFD: An Efficient Double-Cycle Approximation of Functional Dependencies

Code for implementation of [EulerFD: An Efficient Double-Cycle Approximation of Functional Dependencies]().

**Please cite the following work if you use this benchmark or the provided tools or implementations:**

```
@inproceedings{DBLP:conf/icde/LinGSL00WPWL23,
  author       = {Qiongqiong Lin and
                  Yunfan Gu and
                  Jingyan Sai and
                  Jinfei Liu and
                  Kui Ren and
                  Li Xiong and
                  Tianzhen Wang and
                  Yanbei Pang and
                  Sheng Wang and
                  Feifei Li},
  title        = {EulerFD: An Efficient Double-Cycle Approximation of Functional Dependencies},
  booktitle    = {39th {IEEE} International Conference on Data Engineering, {ICDE} 2023,
                  Anaheim, CA, USA, April 3-7, 2023},
  pages        = {2878--2891},
  publisher    = {{IEEE}},
  year         = {2023},
  url          = {https://doi.org/10.1109/ICDE55515.2023.00220},
  doi          = {10.1109/ICDE55515.2023.00220},
  timestamp    = {Sun, 06 Aug 2023 16:12:39 +0200},
  biburl       = {https://dblp.org/rec/conf/icde/LinGSL00WPWL23.bib},
  bibsource    = {dblp computer science bibliography, https://dblp.org}
}
```


### Prerequisites

- Java


### Packages

- Bitset - interfaces and classes for computation of FDs
- Helper - interfaces and classes used by EulerFD
- EulerFD.java - approximate discovery algorithm EulerFD
- Sampling.java - the sampling algorithm of EulerFD


### Datasets

Datasets used in the experiments can be found in: [UCI Machine Learning Repository](http://archive.ics.uci.edu/ml).
