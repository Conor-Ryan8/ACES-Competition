package jlr.acesv1;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Socket
{
    private final static int PACKETSIZE = 11;
    static DatagramSocket socket = null;
    int Read = 1;
    int port;

    public Socket(int port)
    {
        this.port = port;
        try
        {
            socket = new DatagramSocket(port);
        }
        catch (Exception e)
        {
            Log.d("SOCKETERROR","Socket Creation Failed!");
        }
        getData();
    }
    private void getData()
    {
        byte[] receive = new byte[11];
        while (Read == 1)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(receive, PACKETSIZE);
                socket.receive(packet);
                String data = new String(packet.getData());
                Log.d("WARNSSLEEP","GOTDATA!: "+ data);
                String[] parts = data.split(",");
                MainActivity.EyeT = Integer.parseInt(parts[0]);
                MainActivity.Seat = Integer.parseInt(parts[1]);
                MainActivity.WheelL = Integer.parseInt(parts[2]);
                MainActivity.WheelR = Integer.parseInt(parts[3]);
                MainActivity.Actr = Integer.parseInt(parts[4]);
                MainActivity.Brake = Integer.parseInt(parts[5]);
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
    }
    public void close()
    {
        Read = 0;
        try
        {
            socket.close();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
