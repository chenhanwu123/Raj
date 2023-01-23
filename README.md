# 虾皮外卖

## 一. 项目介绍

本项目(虾皮外卖)是专门为餐饮企业(餐厅、饭店)定制的一款软件产品，包括系统管理后台和移动端应用两部分。

其中系统管理后台主要提供给餐饮企业内部员工使用，可以对餐厅的菜品、套餐、订单等进行管理维护。

移动端应用主要提供给消费者使用，可以在线浏览菜品、添加购物车、下单等。

本项目共分为3期进行开发：

第一期主要实现基本需求，其中移动端应用通过H5实现，用户可以通过手机浏览器访问。
第二期主要针对移动端应用进行改进，使用微信小程序实现，用户使用起来更加方便。
第三期主要针对系统进行优化升级，提高系统的访问性能。
## 二. 产品原型
项目成型前的一个简单的布局框架结构图，对于初步的需求有一个可视化的展示。

通过原型展示可以更直观了解项目中的需求，当然产品原型只是一个初步的结构，并不代表最终效果。

资料获取可以直接评论区CALL我。或者跳转到此页面，评论区获取。

## 三. 技术架构

![ecd6c7834c37419c81b7c04f06ad74ca](C:\Users\15771\Desktop\新建文件夹\ecd6c7834c37419c81b7c04f06ad74ca.png)

项目属于前后端分离，前端使用H5页面以及vue框架构建页面，或者使用微信小程序开发。

网关使用的是Nginx代理服务器作为地址的转发，以及集群的Tomcat配置分发。

应用层主要就是使用Spring一系列技术，SpringSession是用来解决集群session共享的问题，数据层使用的MyBatis与数据库交互。

代码管理工具使用的是Git以及Maven仓库。

## 四. 功能描述

![37914c51109e40a88e2fbf111d4c5656](C:\Users\15771\Desktop\新建文件夹\37914c51109e40a88e2fbf111d4c5656.png)

 移动端前台主要是使用H5页面以及微信小程序搭建前端页面，主要功能有：

* 手机号登录
* 微信登录
* 历史订单 
* 菜品规格 
* 购物车 
* 下单 
* 菜品浏览 

**系统后台主要实现功能：**

* 分类管理
* 菜品管理
* 套餐管理
* 菜品口味管理
* 员工管理
* 订单管理

## 五. 项目角色
角色

后台系统管理员：拥有最高权限。
后台普通员：对菜品的管理。
C端用户：登录应用，点餐，下单。
**附录：项目成果图展示**

![20f6a92f8cff4b5b9d93e38dd13c69b3](C:\Users\15771\Desktop\新建文件夹\20f6a92f8cff4b5b9d93e38dd13c69b3.png)

![9308f8d4050145e494f98bf54855e36c](C:\Users\15771\Desktop\新建文件夹\9308f8d4050145e494f98bf54855e36c.png)

![51558d94a5dd4e24bd272d6dbbd1304a](C:\Users\15771\Desktop\新建文件夹\51558d94a5dd4e24bd272d6dbbd1304a.png)