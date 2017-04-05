package com.cdfortis.chensync;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import java.util.List;

/**
 * Created by Diuy on 2017/3/29.
 * FileClient
 */

public class FileClient {

    private static final String TAG = "FileClient";
    private String ip;
    private final int port;
    private final String device;
    private ProgressCallback progress;
    private static final int HEAD_SIZE = 8;
    private static final int TYPE_CHECK = 1;
    private static final int TYPE_UPLOAD = 2;

    interface ProgressCallback {
        void onProgress(int percent);
    }

    public FileClient(String ip, int port, String device,ProgressCallback progress) {

        this.ip = ip;
        this.port = port;
        this.device = device;
        this.progress = progress;
    }

    private byte[] createCheckBody(String folder, List<FileInfo> fileInfos) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, "GBK"));
            pw.println(device + " " + folder);
            for (FileInfo fileInfo : fileInfos) {
                pw.println(fileInfo.path + " " + fileInfo.fileSize + " " + fileInfo.modifyTime);
            }
            pw.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("create check body fail", e);
        } finally {
            safeClose(pw);
        }
    }

    private byte[] createHead(int type, int size) {
        byte[] bytes = new byte[HEAD_SIZE];
        bytes[0] = (byte) 0xCC;
        bytes[1] = (byte) 0xBB;
        bytes[2] = (byte) type;
        bytes[3] = 0;

        byte[] sizeBytes = intToByteArray(size);
        System.arraycopy(sizeBytes, 0, bytes, 4, 4);
        return bytes;
    }


    private List<String> checkFileResult(InputStream is) {
        byte[] headBytes = new byte[HEAD_SIZE];
        try {
            if (is.read(headBytes) != HEAD_SIZE)
                throw new IllegalStateException("read head fail");
        } catch (IOException e) {
            throw new IllegalStateException("read head fail", e);
        }


        if (headBytes[0] != (byte) 0xcc || headBytes[1] != (byte) 0xee)
            throw new IllegalStateException("sync head is error");

        int result = headBytes[2];
        int size = byteArrayToInt(headBytes, 4);
        if (size <= 0 || size > 1024 * 1024 * 10)
            throw new IllegalStateException("bad body size:" + size);

        byte[] bodyBytes = new byte[size];
        int recvSize = 0;
        while (recvSize < size) {
            int ret = 0;
            try {
                ret = is.read(bodyBytes, recvSize, size - recvSize);
            } catch (Exception e) {
                throw new IllegalStateException("read body data fail", e);
            }
            if (ret <= 0)
                throw new IllegalStateException("read body data fail");
            recvSize += ret;
        }


        if (result != 0) {
            String reason;
            try {
                reason = new String(bodyBytes, "GBK");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("create string fail ", e);
            }
            throw new IllegalStateException("result error:" + result + "\n" + reason);
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bodyBytes), "GBK"));
            List<String> files = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                files.add(line);
            }
            return files;
        } catch (Exception e) {
            throw new IllegalStateException("parse result fail", e);
        } finally {
            safeClose(br);
        }
    }

    List<String> checkFile(String folder, List<FileInfo> fileInfos) {
        if (TextUtils.isEmpty(ip)) {
            throw new IllegalArgumentException("ip is empty");
        }
        if (port <= 0 || port >= 65535) {
            throw new IllegalArgumentException("port error");
        }

        if (TextUtils.isEmpty(folder)) {
            throw new IllegalArgumentException("folder ip is empty");
        }
        if (fileInfos == null || fileInfos.size() <= 0) {
            throw new IllegalArgumentException("fileInfos ip is empty");
        }

        byte[] bodyBytes = createCheckBody(new File(folder).getName(), fileInfos);
        byte[] headBytes = createHead(TYPE_CHECK, bodyBytes.length);

        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;

        try {
            try {
                socket = new Socket(ip, port);
            } catch (Exception e) {
                throw new IllegalStateException("connect fail", e);
            }
            try {
                os = socket.getOutputStream();//字节输出流
                os.write(headBytes);
                os.write(bodyBytes);
                os.flush();
            } catch (Exception e) {
                throw new IllegalStateException("write data fail", e);
            }

            try {
                is = socket.getInputStream();
            } catch (Exception e) {
                throw new IllegalStateException("read data fail", e);
            }

            return checkFileResult(is);
        } finally {
            safeClose(is);
            safeClose(os);
            safeClose(socket);
        }
    }

    private byte[] createUploadBody(String folder, FileInfo fileInfo) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, "GBK"));
            pw.print(device + " " + folder + " " + fileInfo.path + " " + fileInfo.fileSize + " " + fileInfo.modifyTime);
            pw.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("OutputStreamWriter create fail", e);
        } finally {
            safeClose(pw);
        }
    }

    private long uploadFileResult(ByteBuffer byteBuffer) {
        byte[] bytes = byteBuffer.array();
        int offset = byteBuffer.arrayOffset();

        if (bytes[offset] != (byte) 0xcc || bytes[offset + 1] != (byte) 0xee)
            throw new IllegalStateException("sync head is error");

        int result = bytes[offset + 2];
        int size = byteArrayToInt(bytes, offset + 4);
        if (size <= 0 || size > 1024 * 4)
            throw new IllegalStateException("bad body size:" + size);

        if (byteBuffer.remaining() < size + HEAD_SIZE)
            return 0;//body数据还未到

        if (result != 0) {
            String reason;
            try {
                reason = new String(bytes, offset + HEAD_SIZE, size, "GBK");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("create string fail", e);
            }
            throw new IllegalStateException("result error:" + result + "\n" + reason);
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes, offset + HEAD_SIZE, size), "GBK"));
            String line = br.readLine();
            return Long.parseLong(line);
        } catch (Exception e) {
            throw new IllegalStateException("parse upload result fail", e);
        } finally {
            byteBuffer.position(size + HEAD_SIZE);
            safeClose(br);
        }
    }

    private void readFile(InputStream inputStream, ByteBuffer byteBuffer) throws IOException {
        int offset = byteBuffer.arrayOffset() + byteBuffer.position();
        int size = byteBuffer.remaining();
        int ret = inputStream.read(byteBuffer.array(), offset, size);
        if (ret <= 0)
            throw new IOException("read file fail:" + ret);

        byteBuffer.position(byteBuffer.position() + ret);
    }

    void uploadFile(String folder, FileInfo fileInfo) {
        if (TextUtils.isEmpty(folder))
            throw new IllegalArgumentException("folder is empty");

        if (fileInfo == null)
            throw new IllegalArgumentException("fileInfo is null");
        if (fileInfo.fileSize <= 0 || TextUtils.isEmpty(fileInfo.path))
            throw new IllegalArgumentException("fileInfo error");

        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(new File(folder, fileInfo.path));
        } catch (Exception e) {
            throw new IllegalStateException("open fail fail:" + new File(folder, fileInfo.path).getAbsolutePath());
        }

        byte[] bodyBytes = createUploadBody(new File(folder).getName(), fileInfo);
        byte[] headBytes = createHead(TYPE_UPLOAD, bodyBytes.length);

        Selector selector = null;
        SocketChannel socketChannel = null;
        SelectionKey selectionKey;
        try {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.socket().setSendBufferSize(1024 * 1024);
                socketChannel.socket().setReceiveBufferSize(10 * 1024);
                socketChannel.socket().setSoTimeout(10000);
                socketChannel.connect(new InetSocketAddress(ip, port));
                // 打开并注册选择器到信道
                selector = Selector.open();
                selectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
                selector.select(10000);
                socketChannel.finishConnect();
                selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
                //selectionKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            } catch (Exception e) {
                throw new IllegalStateException("connect fail:" + ip, e);
            }

            long fileSize = fileInfo.fileSize;
            long sendSize = 0;
            int ret;
            long recvSize = 0;
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024 * 10);
            ByteBuffer readBuffer = ByteBuffer.allocate(1024 * 4 + HEAD_SIZE);

            writeBuffer.put(headBytes);
            writeBuffer.put(bodyBytes);
            writeBuffer.flip();
            boolean sendRequest = true;
            int percent = 0;
            while (recvSize < fileSize) {
                try {
                    selector.select();
                } catch (Exception e) {
                    throw new IllegalStateException("select fail", e);
                }

                if (selectionKey.isWritable()) {
                    if (sendRequest) {
                        try {
                            socketChannel.write(writeBuffer);
                        } catch (Exception e) {
                            throw new IllegalStateException("send data fail", e);
                        }
                        if (!writeBuffer.hasRemaining()) {
                            writeBuffer.clear();
                            sendRequest = false;
                        }
                    } else if (sendSize < fileSize) {
                        try {
                            readFile(fileInputStream, writeBuffer);
                        } catch (Exception e) {
                            throw new IllegalStateException("read file fail", e);
                        }
                        writeBuffer.flip();
                        try {
                            ret = socketChannel.write(writeBuffer);
                            if (ret > 0) {
                                sendSize += ret;
                            }
                        } catch (Exception e) {
                            throw new IllegalStateException("send file data fail", e);
                        }
                        writeBuffer.compact();
                    } else {
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
                if (selectionKey.isReadable()) {
                    try {
                        socketChannel.read(readBuffer);
                    } catch (Exception e) {
                        throw new IllegalStateException("read data fail", e);
                    }
                    if (readBuffer.position() < HEAD_SIZE)
                        continue;
                    readBuffer.flip();
                    long result = uploadFileResult(readBuffer);
                    if (result > 0) {
                        recvSize = result;
                        int p = (int) Math.floor(recvSize * 100 / fileSize);
                        if (p > percent) {
                            percent = p;
                            if(progress!=null) progress.onProgress(percent);
                            Log.d(TAG, "recv percent:" + percent);
                        }
                    }
                    readBuffer.compact();
                }
            }
        } finally {
            safeClose(fileInputStream);
            safeClose(selector);
            safeClose(socketChannel);
        }
    }

    private static int byteArrayToInt(byte[] b, int offset) {
        return b[offset] & 0xFF |
                (b[offset + 1] & 0xFF) << 8 |
                (b[offset + 2] & 0xFF) << 16 |
                (b[offset + 3] & 0xFF) << 24;
    }

    private static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) (a & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 24) & 0xFF)
        };
    }

    private static void safeClose(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
