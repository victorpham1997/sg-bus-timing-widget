package com.xsteinlab.sgbustimingwidget.network;

import android.util.Log;

import com.xsteinlab.sgbustimingwidget.utils.Utils;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.text.ParseException;

public abstract class ApiCallback extends UrlRequest.Callback{

    private static final int BYTE_BUFFER_CAPACITY_BYTES = 64 * 1024;
    private final ByteArrayOutputStream bytesReceived = new ByteArrayOutputStream();
    private final WritableByteChannel receiveChannel = Channels.newChannel(bytesReceived);

    private static final String TAG = "MyApiRequestCallback";
    private final long startTimeNanos;


    public ApiCallback(){
        startTimeNanos = System.nanoTime();
    }


    @Override
    public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) throws Exception {
        Utils.localLogging(TAG, "onRedirectReceived method called.");
        request.followRedirect();
    }

    @Override
    public void onResponseStarted(UrlRequest request, UrlResponseInfo info) throws Exception {
        Utils.localLogging(TAG, "onResponseStarted method called.");
        Utils.localLogging(TAG, info.getUrl());
//        bytesReceived.reset();
        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES));
    }

    @Override
    public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) throws Exception {
        Utils.localLogging(TAG, "onReadCompleted method called.");
//        Utils.localLogging(TAG, info.getUrl());
        byteBuffer.flip();
        try {
            receiveChannel.write(byteBuffer);
        } catch (IOException e) {
            Utils.localLogging(TAG, "IOException during ByteBuffer read. Details: " + e);
        }
        byteBuffer.clear();
        request.read(byteBuffer);
    }

    @Override
    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
        Utils.localLogging(TAG, "onSucceeded method called.");

        long latencyNanos = System.nanoTime() - startTimeNanos;

        byte[] bodyBytes = bytesReceived.toByteArray();
        String packet = new String(bodyBytes);
        Utils.localLogging("Packet received " + packet);

        try{
            JSONObject respJo = new JSONObject(packet);
            postSucceeded(request, info, respJo);

        } catch (
                JSONException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {
    }

    public abstract void postSucceeded(
            UrlRequest request, UrlResponseInfo info, JSONObject respJo) throws JSONException, ParseException;

}
