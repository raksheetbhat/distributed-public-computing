# distributed-public-computing
Distributed Public Computing and Storage using Mobile Devices

As the processing power and storage capacities of mobile phones increase, combined with their huge numbers and ubiquitous nature, they open new possibilities in the field of public resource computing, also called volunteer computing. An effective volunteer computing solution can be realized by utilizing the idle CPU cycles and free storage space of these mobile phones. Existing solutions like BOINC cater mainly to large organizations and have complex procedures for submitting datasets and code for computation.
Here we propose a novel distributed computing platform which enables any user to upload a dataset, along with the Java code that needs to be run on it, and the merge code that combines the results. We have come up with a distribution and scheduling algorithm which leverages the computational heterogeneity of the devices, the complexity of the task involved and the size of the dataset uploaded.

Our platform also provides decentralized public storage, using which users can upload any file securely. The platform uses threshold cryptography on the uploaded files to create encrypted shares, which reduces the redundancy required to maintain integrity, and also accounts for the unreliability of mobile device availability.
We have run a DNA sequence similarity algorithm on our system, utilizing a number of Android phones of different makes. Our results show that this approach is a viable, cost-efficient alternative to traditional distributed computing resources for performing non-time bound computations on large datasets.

Link to published paper: https://ieeexplore.ieee.org/document/8674111
