/*
 * Copyright 2014 jts
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.jts.authserver.network.serverpackets;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ru.jts.authserver.network.Client;
import ru.jts.authserver.network.crypt.CryptEngine;
import ru.jts.common.network.udp.ServerPacket;
import ru.jts.common.util.ArrayUtils;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Camelion
 * @date: 20.12.13/1:36
 */
public class AuthorizeResponse extends ServerPacket<Client> {
	private final short sessionId;
	private ByteBuf buf;

	public AuthorizeResponse(short sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	protected void before() throws Exception {
		buf = Unpooled.buffer().order(ByteOrder.LITTLE_ENDIAN);

		InetSocketAddress address = getClient().getServerAddress();

		buf.writeBytes(address.getAddress().getAddress());
		//buf.writeBytes(new byte[]{94, 120, 1, 1});
		buf.writeInt(address.getPort());

		buf.writeInt(getClient().getRandomKey());

		Map<String, String> jsonMap = new HashMap<>();
		//jsonMap.put("security_msg", "old_pass");
		jsonMap.put("token2", getClient().generateToken2());

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(jsonMap);
		buf.writeByte(json.length());
		buf.writeBytes(json.getBytes());

		byte[] cryptedData = CryptEngine.getInstance().encrypt(buf.copy().array(), getClient().getBlowFishKey(), CryptEngine.ZERO_TRAILING_MODE);

		buf.clear();
		buf.writeBytes(cryptedData);
	}

	@Override
	protected void writeImpl() {
		writeShort(sessionId);
		writeBytes(0x00, 0x00);
		writeByte(0x01); // LOGIN_OK
		writeBytes(buf);

		System.out.println(ArrayUtils.bytesToHexString(getClient().getBlowFishKey()));

		// crypted with CryptEngine
		// writeBytes(127, 0, 0, 1); // доп. ип игрового сервера
		// writeInt(12313); // открытый для клиента порт игрового сервера
		// writeInt(unk); 68 19 3F 5F // проверочный ключ, рандомный, клиент отсылает его обратно в следующем пакете
		// writeShort(json msg size);
		// writeString(json msg); {"security_msg":"old_pass","token2":"8531071:5661541700570227003:134709503890988403063748623096524483879"} 
		// writeByte(0x00);
	}
}
