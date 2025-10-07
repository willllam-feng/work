# S-DES Java加解密实验一 

## 简介

本实验使用 Java 语言配合 Swing 框架实现了简化数据加密标准（S-DES），并提供了一个可视化界面，方便对 8 位明文与 10 位密钥进行加密、解密实验。


<img width="796" height="679" alt="e55763722e51a9cd814ed30af4582d7b" src="https://github.com/user-attachments/assets/dfd54632-0f41-4f47-94aa-f9be7ec7cf87" />



## 第一关

根据S-DES算法编写和调试程序，提供GUI解密支持用户交互。输入是8bit的数据和10bit的密钥，输出是8bit的密文或明文。单一密钥进行加密和解密。

<img width="797" height="192" alt="c7559a2404b02ceb7f027a33066d1023" src="https://github.com/user-attachments/assets/d73ee7a1-d023-489a-8e6b-ff57ce8b708b" />

<img width="793" height="203" alt="e3b45863e2826847ec11955470fd1ad5" src="https://github.com/user-attachments/assets/0ef1e7bc-965e-450d-9591-6cf01e6b6c51" />


## 第二关

考虑到是**算法标准**，所有人在编写程序的时候需要使用相同算法流程和转换单元(P-Box、S-Box等)，以保证算法和程序在异构的系统或平台上都可以正常运行。

设有A和B两组位同学(选择相同的密钥K)；则A、B组同学编写的程序对明文P进行加密得到相同的密文C；或者B组同学接收到A组程序加密的密文C，使用B组程序进行解密可得到与A相同的P。

参考别人的明文01000001，密钥1111111111，加密后的01110100在我们的程序上成功解密为01000001

<img width="796" height="216" alt="cd4e34169d52dbfc1c35544eb95a75a9" src="https://github.com/user-attachments/assets/1efc4762-4e53-4256-928b-e330f30a5b27" />

<img width="799" height="204" alt="861bff15b43fbb0cee39a9f344c3ae6f" src="https://github.com/user-attachments/assets/cfd4ef2e-c6af-46bd-87e4-689b877c7304" />

## 第三关

考虑到向实用性扩展，加密算法的数据输入可以是ASII编码字符串(分组为1 Byte)，对应地输出也可以是ACII字符串(很可能是乱码)。

在明文为`lovecqu`，密钥为`1111111111`的加密，解密结果如下。

<img width="794" height="205" alt="260a9614a206f5a69dea098a91fe3503" src="https://github.com/user-attachments/assets/58010a0d-d642-4776-90a9-315761fbf26f" />

<img width="797" height="205" alt="235b510818eda13051b58350b4235ef0" src="https://github.com/user-attachments/assets/a3504efd-6670-47c4-becc-1f874a2d6cd2" />



## 第四关

假设你找到了使用相同密钥的明、密文对(一个或多个)，请尝试使用暴力破解的方法找到正确的密钥Key。在编写程序时，你也可以考虑使用多线程的方式提升破解的效率。请设定时间戳，用视频或动图展示你在多长时间内完成了暴力破解。

在明文为`10101010`，密文为`11111111`，暴力破解结果如下，时间消耗为0.014s

<img width="829" height="281" alt="d6c917850f0c367632ff88747301216b" src="https://github.com/user-attachments/assets/49640352-c293-4520-ab58-ea7767d47d25" />


## 第五关

根据第4关的结果，进一步分析，对于你随机选择的一个明密文对，是不是有不止一个密钥Key？进一步扩展，对应明文空间任意给定的明文分组$P_n$，是否会出现选择不同的密钥$K_{i}\ne K_{j}$加密得到相同密文$C_n$的情况？

- **问题一：** 随机选择的明密文对下，存在 6 个可能的密钥 Key。
- **问题二：** 对于任意明文分组 $P_n$，确实可能出现不同密钥 $K_{i}\ne K_{j}$ 加密得到同一密文 $C_n$ 的情况。
  
<img width="829" height="281" alt="d6c917850f0c367632ff88747301216b" src="https://github.com/user-attachments/assets/b9537230-0125-48ba-aeec-9ed23c043858" />

<img width="835" height="275" alt="7519e44a143235f054c60fe68a3bb7b3" src="https://github.com/user-attachments/assets/114594e2-a237-4820-bef3-ef46b6b289d6" />

根据测试得出对于任意明文分组 $P_n$，确实可能出现不同密钥 $K_{i}\ne K_{j}$ 加密得到同一密文 $C_n$ 的情况。


## 快速开始

- **启动**：在项目根目录运行SDesGUI.Java
