#!/usr/bin/python3

#数据库连接模块 
import pymysql
import datetime

#共享单车系统 网络交互 功能模块
from socket import *
from time import ctime

db=""
cursor=""
def db_connect():
    global db
    global cursor
    
    # 打开数据库连接
    db = pymysql.connect(host="67.209.186.100",user="root",passwd="xxxxxx",db="mysql")
 
    # 使用 cursor() 方法创建一个游标对象 cursor
    #使用cursor()方法获取操作游标
    cursor = db.cursor()
 
    # 使用 execute()  方法执行 SQL 查询 
    cursor.execute("SELECT VERSION()")
 
    # 使用 fetchone() 方法获取单条数据.
    data = cursor.fetchone()
 
    print ("Database version : %s " % data)

    db = pymysql.connect(host="67.209.186.100",user="root",passwd="xxxxxx",db="db_sharedbike")
    cursor = db.cursor()
    

def db_dis_connect(): 
    global db
    # 关闭数据库连接
    try:
        db.close()
    except Exception as e:
        print("断开数据库失败，请先连接数据库！！！")
        print(e)


def newtwork_activate():
    HOST = ""
    PORT = 10001
    BUFSIZ = 1024
    ADDR = (HOST, PORT)


    tcpSerSock = socket(AF_INET, SOCK_STREAM)
    tcpSerSock.bind(ADDR)
    tcpSerSock.listen(10)

 
    while True:
        print("服务器启动， waiting for connection...")
        db_connect()
        print("数据库已连接！！！")
        tcpCliSock, addr = tcpSerSock.accept()
        print("本次网络连接已经成功建立！！！")
        print("connected from :", addr)

        try:
            #防止网络攻击，扫描等发送非法请求
            data = tcpCliSock.recv(BUFSIZ).decode("utf-8")
            recv_message_list=data.split()
        except Exception as e:
            print(e)
            continue
            

        print(recv_message_list)


        if(recv_message_list==[]):
            print("网络中断")
        elif(recv_message_list[0]=="bike_num"):
            ""
            print(recv_message_list)
            fun_validate(tcpCliSock, recv_message_list)

        db_dis_connect()
        print("数据库已经断开连接！！！")

    tcpSerSock.close()    

    

def fun_validate(tcpCliSock, recv_message_list):
    #判断自行车是否存在，存在是否可以开启
    state_2=0
    def select_bike_state():
        nonlocal state_2
        sql="select state from bike_info where id='%d'"%(int(recv_message_list[1]))

        try:
            cursor.execute(sql)
            result=cursor.fetchone()

            print(result)
            
            if result==None:
                #没有该车
                print(0)
                state_2=0
            if result[0]==1:
                #可以借车
                print(1)
                state_2=2
            else:
                #不可以借车
                print(2)
                state_2=1
        except Exception as e:
            #数据库连接发生错误
            state_2=0
            print ("Error: unable to fetch data")
            print (e)

    select_bike_state()

    message=state_2+'\r\n'
    
    tcpCliSock.sendall(message.encode('utf-8'))
    tcpCliSock.close()


if __name__=="__main__":
    newtwork_activate()
    

    
