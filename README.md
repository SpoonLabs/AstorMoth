# AstorMoth


AstorMoth contains automated program repair approaches based on the Dynamoth synthesis engine.

AstorMoth is built on top of [Astor](https://github.com/SpoonLabs/astor) and [Dynamoth](https://github.com/SpoonLabs/nopol)

If you use AstorMoth, please site:

```
@inproceedings{10.1145/2931037.2948705,
author = {Martinez, Matias and Monperrus, Martin},
title = {ASTOR: A Program Repair Library for Java (Demo)},
year = {2016},
isbn = {9781450343909},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/2931037.2948705},
doi = {10.1145/2931037.2948705},
abstract = { During the last years, the software engineering research community has proposed approaches for automatically repairing software bugs. Unfortunately, many software artifacts born from this research are not available for repairing Java programs. To-reimplement those approaches from scratch is costly. To facilitate experimental replications and comparative evaluations, we present Astor, a publicly available program repair library that includes the implementation of three notable repair approaches (jGenProg, jKali and jMutRepair). We envision that the research community will use Astor for setting up comparative evaluations and explore the design space of automatic repair for Java. Astor offers researchers ways to implement new repair approaches or to modify existing ones. Astor repairs in total 33 real bugs from four large open source projects. },
booktitle = {Proceedings of the 25th International Symposium on Software Testing and Analysis},
pages = {441–444},
numpages = {4},
keywords = {Automated software repair, software defects},
location = {Saarbr\"{u}cken, Germany},
series = {ISSTA 2016}
}
```
 

```
@misc{yu2019learning,
      title={Learning the Relation between Code Features and Code Transforms with Structured Prediction}, 
      author={Zhongxing Yu and Matias Martinez and Tegawendé F. Bissyandé and Martin Monperrus},
      year={2019},
      eprint={1907.09282},
      archivePrefix={arXiv},
      primaryClass={cs.SE}
}
```


```
@inproceedings{10.1145/2896921.2896931,
author = {Durieux, Thomas and Monperrus, Martin},
title = {DynaMoth: Dynamic Code Synthesis for Automatic Program Repair},
year = {2016},
isbn = {9781450341516},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/2896921.2896931},
doi = {10.1145/2896921.2896931},
abstract = {Automatic software repair is the process of automatically fixing bugs. The Nopol repair system [4] repairs Java code using code synthesis. We have designed a new code synthesis engine for Nopol based on dynamic exploration, it is called DynaMoth. The main design goal is to be able to generate patches with method calls. We evaluate DynaMoth over 224 of the Defects4J dataset. The evaluation shows that Nopol with DynaMoth is capable of synthesizing patches and enables Nopol to repair new bugs of the dataset.},
booktitle = {Proceedings of the 11th International Workshop on Automation of Software Test},
pages = {85–91},
numpages = {7},
location = {Austin, Texas},
series = {AST '16}
}
```



## Contact:

Matias Martinez, Martin Monperrus


