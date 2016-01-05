# geister_server

Geister用のゲームサーバ(作りかけ)．

## 動作概要(仮)
- 二つのクライアントとSocketで通信し，Geisterを進行する
- ボードの管理，勝敗の管理をする

## 盤面(仮)
        0 1 2 3 4 5
      0   h g f e
      1   d c b a
      2
      3
      4   A B C D
      5   E F G H
- サーバ側では表示のy=0の側を先手番の陣，y=5の側を後手番の陣とする
- プレーヤーは自分が先手なのか後手なのかによらず，y=5の側が自陣であるとする
- コマの名前は，ゲーム開始から終了まで変わらない
- コマが赤/青はゲーム開始前に，それぞれのクライアントに設定してもらう

## ゲームの進行(仮)
- サーバー: python socket_server.pyで2つのプレーヤインスタンス(それぞれ8個のコマを持つ)を生成してクライアントからの通信を待つ
 - 先手のクライアントは10000番，後手のクライアントは10001番で待ち受ける
- クライアント: クライアントは先手/後手に応じて10000番/10001番に接続．recvして"SET?"という文字列を受信する
- クライアント: クライアントは赤オバケを4つセットし，recvする
 - 赤オバケをセットするコマンドは，SET:ABCD\r\n
 - 正しいコマンドでない場合はNG\r\nが返る
 - 正しいコマンドであった場合，次に盤面情報が送られてくるまで待つことになる
- サーバー: 2つのクライアントが赤のコマをセットし終えると，サーバーは，先手番のクライアントに盤面情報を送ると共に手の入力を待ち受ける．
 - 盤面情報はコマの(x,y)と色(相手に非公開の赤/青=R/B，両者が見えている赤=r/青=b/不明=u)を ABCDEFGHabcdefgh (ここで小文字は相手のコマ)順に，並べたもの
 - たとえば初期状態は，14R24R34R44R15B25B35B45B41u31u21u11u40u30u20u10u
 - 取られたコマの座標は(9,9)
 - 盤外に逃げ出したコマンドの座標は(8,8)
 - 取った/取られたコマおよび盤外のコマ色は公開される．
 - 自分の手番によらずどちらのクラントも同じ方向からのビューを持つ(5の側が自陣，0の側が相手陣)
- クライアント: 先手番のクライアントは手を打ち，recvする．
 - 手を打つには， MOV:A,NORTH\r\n のように動かすコマ名と方角を送る
 - 方角はNORTH/EAST/WEST/SOUTHの4種類
 - 正しいコマンドでない場合はNGが返る
 - 正しいコマンドであった場合，次に盤面情報が送られてくるまで待つことになる
- サーバー: サーバーは先手番の手を受理したら，後手番のクライアントに更新後の盤面情報を送る
- クライアント: 後手番のクライアントは手を打ち，recvする．
- サーバー: 先手/後手のどちらかで勝負がついたら，両方に結果と終了時点での盤面情報を送る．
 - 勝った方にはWON，負けた方にはLSTを送る．

## 実行例
### サーバー
    python socket_server.py

### クライアント0(先手番)/iPythonでインタラクティブ入力の例:
    In [5]: s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    In [6]: s.connect(("localhost", 10000)); s.recv(4096)
    Out[6]: b'SET?\r\n'
    In [7]: s.send(b"SET:EFGH\r\n"); s.recv(4096) # 赤のコマをセット．クライアント1のSETコマンド完了までブロック
    Out[7]: b'MOV?14B24B34B44B15R25R35R45R41u31u21u11u40u30u20u10u\r\n' # クライアント1のSETコマンド(In[5])の結果，サーバーから送られてきた
    In [8]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096) # コマAを北に一つすすめる．クライアント1のMOVコマンド完了までブロック
    Out[8]: b'MOV?13B24B34B44B15R25R35R45R42u31u21u11u40u30u20u10u\r\n' # クライアントのMOVコマンド(IN[6]) の結果，サーバーから送られてきた
    In [9]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[9]: b'MOV?12B24B34B44B15R25R35R45R43u31u21u11u40u30u20u10u\r\n'
    In [10]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[10]: b'MOV?11B24B34B99b15R25R35R45R44u31u21u99r40u30u20u10u\r\n'
    In [11]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[11]: b'MOV?10B24B34B99b15R25R35R99r45u31u21u99r40u30u20u99b\r\n'
    In [12]: s.send(b"MOV:A,WEST\r\n"); s.recv(4096)
    Out[12]: b'MOV?00B24B34B99b15R25R35R99r55u31u21u99r40u30u20u99b\r\n'
    In [13]: s.send(b"MOV:A,WEST\r\n"); s.recv(4096)
    Out[13]: b'WON:88b24B34B99b15R25R35R99r55u31u21u99r40u30u20u99b\r\n' # 勝負がついた．こちらの勝ちと終局情報

### クライアント1(後手番)/iPythonでインタラクティブ入力の例:
    In [2]: import socket
    In [3]: s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    In [4]: s.connect(("localhost", 10001)); s.recv(4096)
    Out[4]: b'SET?\r\n'
    In [5]: s.send(b"SET:ABCD\r\n"); s.recv(4096) # # 赤のコマをセット．クライアント0のMOVコマンド完了までブロック
    Out[5]: b'MOV?14R24R34R44R15B25B35B45B42u31u21u11u40u30u20u10u\r\n' # クライアント0のMOVコマンド(In[8])の結果，サーバーから送られてきた
    In [6]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096) # コマAを北に一つすすめる．クライアント0のMOVコマンド完了までブロック
    Out[6]: b'MOV?13R24R34R44R15B25B35B45B43u31u21u11u40u30u20u10u\r\n'
    In [7]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[7]: b'MOV?12R24R34R99r15B25B35B45B44u31u21u11u40u30u20u10u\r\n'
    In [8]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[8]: b'MOV?11R24R34R99r15B25B35B99b45u31u21u99b40u30u20u10u\r\n'
    In [9]: s.send(b"MOV:A,NORTH\r\n"); s.recv(4096)
    Out[9]: b'MOV?10R24R34R99r15B25B35B99b55u31u21u99b40u30u20u99r\r\n'
    In [10]: s.send(b"MOV:A,WEST\r\n"); s.recv(4096)
    Out[10]: b'LST:00R24R34R99r15B25B35B99b88b31u21u99b40u30u20u99r\r\n' # 勝負がついた．こちらの負けと終局情報
