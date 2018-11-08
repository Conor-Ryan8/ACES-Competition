package jlr.acesv1;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.app.Activity;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    static int Port = 9999;

    LinearLayout L1;

    ImageView pedalimage;
    ImageView seatimage;
    ImageView roadimage;

    Context mContext;
    Activity mActivity;

    public static int WheelL = 0;
    public static int WheelR = 0;
    public static int EyeT = 0;
    public static int Actr = 0;
    public static int Brake = 0;
    public static int Seat = 0;

    int RedWarning = 0;
    int YellowAlert = 0;

    int keepalive = 1;


    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d("STARTUP","Program Starting...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateRoad();
        updateSeat();
        updatePedals();

        mContext = getApplicationContext();
        mActivity = MainActivity.this;

        //Server thread to listen for incoming data
        Thread server = new Thread( new Runnable()
        {
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                Log.d("SOCKETCREATE","Attempting to create Socket...");
                Socket sock = new Socket(Port);
            }
        });
        server.start();

        //Background thread to listen for changes in values and update the correct graphic when needed
        Thread Graphics = new Thread( new Runnable()
        {
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                //Current Values
                int CurrentSeat = Seat;
                int CurrentEyeT = EyeT;
                int CurrentWheelL = WheelL;
                int CurrentWheelR = WheelR;
                int CurrentActr = Actr;
                int CurrentBrake = Brake;

                while (keepalive==1)
                {
                    if (CurrentSeat != Seat)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateSeat();
                            }
                        });
                        CurrentSeat = Seat;
                        Log.d("SEATUPDATE","Updated Seat Graphic!");
                    }
                    if (CurrentEyeT != EyeT || CurrentWheelL != WheelL || CurrentWheelR != WheelR)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updateRoad();
                            }
                        });
                        CurrentEyeT = EyeT;
                        CurrentWheelL = WheelL;
                        CurrentWheelR = WheelR;
                        Log.d("ROADUPDATE","Updated Road Graphic!");
                    }
                    if (CurrentBrake != Brake|| CurrentActr != Actr)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                updatePedals();
                            }
                        });
                        CurrentBrake = Brake;
                        CurrentActr = Actr;
                        Log.d("PEDALSUPDATE","Updated Pedals Graphic!");
                    }
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e)
                    {
                        Log.d("GRAPHICSSLEEP","Sleep error in Graphics Thread!");
                    }
                }
            }
        });
        Graphics.start();


        Thread Warnings = new Thread( new Runnable()
        {
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                int CurrentRedWarning = 0;
                int CurrentYellowAlert = 0;

                while (keepalive==1)
                {
                    if (Actr==1 && EyeT==0 || Actr==1 && Seat==0 || Actr==1 && WheelL==0 && WheelR==0)
                    {
                        RedWarning = 1;
                        YellowAlert = 0;

                        if (CurrentRedWarning != RedWarning)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateBG();
                                }
                            });
                            CurrentRedWarning = RedWarning;
                            CurrentYellowAlert = YellowAlert;
                        }
                    }
                    else if (EyeT==0 || Seat==0 || WheelR==0 || WheelL==0)
                    {
                        RedWarning = 0;
                        YellowAlert = 1;

                        if (CurrentYellowAlert != YellowAlert)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateBG();
                                }
                            });
                            CurrentRedWarning = RedWarning;
                            CurrentYellowAlert = YellowAlert;
                        }
                    }
                    else
                    {
                        YellowAlert = 0;
                        RedWarning = 0;

                        if (CurrentRedWarning != RedWarning || CurrentYellowAlert != YellowAlert)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    updateBG();
                                }
                            });
                            CurrentYellowAlert = YellowAlert;
                            CurrentRedWarning = RedWarning;
                        }
                    }
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e)
                    {
                        Log.d("WARNINGSSLEEP","Sleep error in Warnings Thread!");
                    }
                }
            }
        });
        Warnings.start();

        Thread Alert = new Thread( new Runnable()
        {
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Ringtone alert = RingtoneManager.getRingtone(mContext,uri);
                while (keepalive==1)
                {
                    if (RedWarning == 1 && !alert.isPlaying())
                    {
                        alert.play();
                    }
                    if (RedWarning == 0 && alert.isPlaying())
                    {
                        alert.stop();
                    }
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch(InterruptedException e)
                    {
                        Log.d("ALERTSLEEP","Sleep error in Alert Thread!");
                    }
                }
            }
        });
        Alert.start();
    }

    //Methods to update Wheel & Eye tracker graphics
    public void updateRoad()
    {
        if (WheelL == 0 && EyeT == 0 && WheelR == 0) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road000);
        }
        else if (WheelL == 0 && EyeT == 0 && WheelR == 1) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road010);
        }
        else if (WheelL == 0 && EyeT == 1 && WheelR == 0) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road001);
        }
        else if (WheelL == 0 && EyeT == 1 && WheelR == 1) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road011);
        }
        else if (WheelL == 1 && EyeT == 0 && WheelR == 0) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road100);
        }
        else if (WheelL == 1 && EyeT == 0 && WheelR == 1) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road110);
        }
        else if (WheelL == 1 && EyeT == 1 && WheelR == 0) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road101);
        }
        else if (WheelL == 1 && EyeT == 1 && WheelR == 1) {
            roadimage = findViewById(R.id.roadimage);
            roadimage.setImageResource(R.drawable.road111);
        }
    }
    //Methods to update Pedal Graphics
    public void updatePedals()
    {
        if (Actr ==0 && Brake ==0)
        {
            pedalimage = findViewById(R.id.pedalimage);
            pedalimage.setImageResource(R.drawable.pedals00);
        }
        else if (Actr ==0 && Brake ==1)
        {
            pedalimage = findViewById(R.id.pedalimage);
            pedalimage.setImageResource(R.drawable.pedals10);
        }
        else if (Actr ==1 && Brake ==0)
        {
            pedalimage = findViewById(R.id.pedalimage);
            pedalimage.setImageResource(R.drawable.pedals01);
        }
        else if (Actr ==1 && Brake ==1)
        {
            pedalimage = findViewById(R.id.pedalimage);
            pedalimage.setImageResource(R.drawable.pedals10);
        }
    }
    //Methods to update Seat Graphics
    public void updateSeat()
    {
        if (Seat ==0)
        {
            seatimage = findViewById(R.id.seatimage);
            seatimage.setImageResource(R.drawable.seat0);
        }
        else if (Seat ==1)
        {
            seatimage = findViewById(R.id.seatimage);
            seatimage.setImageResource(R.drawable.seat1);
        }
    }
    //Method to update borders
    public void updateBG()
    {
        //RED WARNING if accelerator is pressed and eye tracker, seat or both wheel sensors are disengaged
        if (RedWarning == 1)
        {
            L1 = findViewById(R.id.LinearLayout1);
            L1.setBackgroundResource(R.drawable.bgwarning);
        }

        //YELLOW alert if any of the seat, eye tracker, or wheel sensors are disengaged
        else if (YellowAlert == 1)
        {
            L1 = findViewById(R.id.LinearLayout1);
            L1.setBackgroundResource(R.drawable.bgalert);
        }
        //NO border
        else
        {
            L1 = findViewById(R.id.LinearLayout1);
            L1.setBackgroundResource(R.drawable.bgnone);
        }
    }
}
