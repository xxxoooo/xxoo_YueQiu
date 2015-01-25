YueQiu
======
目前待解决的问题：
共同问题：
1.ViewPager会一次性创建两个fragment，如果此时没网络，会Toast两次，在屏幕上会感觉Toast一直存在
2.由ViewPager里面的fragment跳转到activity以后，再回来时始终是第一个fragment，应该考虑保存状态，之前是第几个fragment，回来以后仍然是那个fragment
3.searchview 的文字颜色以及hitnicon的修改，目前试了很多种方法，都无效
4.动态显示数据的view在xml里面的默认值都去掉

登录及注册：
1.忘记密码还没做
2.手机验证码也还有问题

侧滑更多：
1.点击删除时重新自定义dialog
2.反馈以及发布活动，填写内容时有个表情按钮，但是当输入内容时，软键盘会弹出覆盖该按钮
图片缓存
3.发布活动的结束时间是否应该限定在跟开始时间同一天？



附近：
1.listview不显示数据，虽然改为match_parent以后可以显示，但是多次滑动以后还会出现不显示的问题
2.代码可以考虑整合一下，多个fragment中用到的方法重复
3.球厅的图片，要显示出来
4.约球的popupwindow，发布时间还有球厅的区域，由于listview数据过多会导致最上方的标题栏不见
5.筛选按钮的高度适当的缩小一点，下面的listview空间有些不够

聊吧：
1.activity跳转动画(fragment之间的切换尚未添加！)
2.联系人，一开始如果网络请求错误，第一个箭头是往下的，获取数据后，点击item，item颜色不变，应该有被按下的效果（解决）
3.htc无法用网络获取地理位置信息，对于这样的情况要处理（已做处理）
4.发送消息界面，应当监听back事件，如果软键盘或表情栏弹出来，此时按back键，应该是让弹出的试图消失，而不是直接将activity finish(已解决)
5.球友管理界面的备注的Edittext显示不了字体，字体颜色也不对(字体颜色已修改，nexus5字体不显示？)
6.附近的人，应该是一开始就获取数据，如果没数据，界面中央也应该有相应的emptyview，progressbar显示时，下方最好加上textview，显示文字（解决）

活动：
1.由于是用一个数据库，在进入详情后会更新数据库，但是当退出详情后，由于会重新向网络请求
，所以获取数据后又会再次更新数据，将本来已经存好的值变为null
2.点击收藏按钮后，如果成功则应该插入数据库
3.详情中现在加入了一个join_list
4.分享的接口
5.长按每个item是否应该有一定的操作？
6.发布活动表情软键盘弹出问题，Edittext插入图片与图片缓存

台球圈：
1.详情页面还没写
2.同样是长按item是否应该有操作？
3.由于用铜的数据库，是不是也会存在更新数据库的问题
4.发表话题的表情，软键盘弹出问题
5.EditText插入图片
6.图片缓存








Android Yueqiu
