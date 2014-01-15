package ru.jts.server.network.serverpackets;

import ru.jts.common.network.udp.ServerPacket;
import ru.jts.server.network.Client;

/**
 * @author: Camelion
 * @date: 20.12.13/1:36
 */
public class AuthorizeByPasswordResponse extends ServerPacket<Client> {
    private final short sessionId;

    public AuthorizeByPasswordResponse(short sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    protected void writeImpl() {
        writeShort(0x00);
        writeByte(0xFF);
        writeByte(0x7D);
        writeBytes(0x00, 0x00, 0x00);
        writeShort(sessionId);
        writeByte(0x00); // 0x01 есть открытая сессия 0x00 - нету
        writeShort(0x01);

        // crypted with CryptEngine
        //writeBytes(127, 0, 0, 1); // доп. ип игрового сервера
        // writeInt(12313); // порт игрового сервера
        // writeInt(unk); 68 19 3F 5F
        // writeByte (json msg size);    {"security_msg":"old_pass","token2":"8531071:5661541700570227003:134709503890988403063748623096524483879"} 
        // writeByte(0x00);
    }
}
