# MAIN SKILL
<br>

>Camera2 API 를 이용한 백그라운드 캠 사용

- 다른 앱을 사용시에도 이 앱이 카메라 동작을 하는것이 목적이다. 
ex) 캠으로 사용자의 행동을 인식하여 Auto Touch 기능 등

>백그라운드 동작을 위해 Service 사용

- 하지만 Oreo이후 startForegroundService를 호출후 5초 이내에 service안에서 startForeground를 호출하지 않으면 crash가 발생하게 된다.

- crash가 발생하지 않게 하려면 startForeground를 호출하면 된다. 

- startForground를 호출하게 되면 상단에 백그라운드 서비스가 사용중 이라는 notification을 사용자에게 보여줘야 한다.

>추가로 카메라 동작이 잘 되는지 확인을 위해 WindowManager를 사용
- WindowManager로 다른 앱 위에서 팝업 창을 이용하여 카메라 동작이 잘 되는지 확인

# Camera2 API
<br>
>Camera2 프로세스 동작

![image](https://user-images.githubusercontent.com/26592310/165225071-598ca2ef-3dc8-4ebf-ba8a-846046dea3a0.png)

1. 카메라 매니져를 얻어온다.

2. 카메라 매니져로, 디바이스에 연결된 카메라 id 리스트를 얻어오고, 각 카메라의 characteristics(특성)를 이용해서 각각의 정보를 비교하며 원하는 카메라를 선정

3. 카메라 아이디를 이용해서 해당 카메라를 조작할수 있는 카메라 디바이스 객체를 가져옴(open을 하면, 해당 디바이스에 접속하여 계속 동작하는 루프 스레드가 생성될수 있는 것.)

4. 결과물을 받아올 Surface(뷰)를 생성

5. 카메라 디바이스 객체로 해당 디바이스에 명령을 내릴수 있는 새로운 스레드인 세션을 만들어달라고 요구하고 세션 객체를 받아옴.

6. 세션 객체에 아까 만든 서페이스를 결합한 request를 받아들여 동작하고 출력값을 서페이스로 받아옴.


# Notification API

- 사용자에게 보여줄 알림을 설정 후 startForeground 하여 앱을 ForeGround로 올림
```java
private void startForeground(){
        Intent intent =new Intent(this,MainActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.app_name))
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.app_name))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID,notification);
    }
```

