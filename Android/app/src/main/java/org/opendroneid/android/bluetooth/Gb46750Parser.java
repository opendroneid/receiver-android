/*
 * Copyright (C) 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * GB 46750-2025《民用无人驾驶航空器系统运行识别规范》运行识别报文解析器。
 *
 * 解析 §5.2.1 图3 / 表1-3 定义的"运行识别信息数据包"格式：
 *   数据类型(1B,=255) | 版本号(1B) | 数据长度(1B) | 数据标识(3+N B, 位掩码) | 数据内容项(可变)
 *
 * 数据标识：每个字节的高 7 位(0x80..0x02)为内容标志位（对应表2的序号 001..021，
 * 每字节 7 项），最低位(0x01)为扩展标志位（=1 表示后续字节仍属数据标识）。
 * 数据内容项按表2序号顺序拼接，每项长度与编码取值见 §5.2.4 表3。
 *
 * 该类不依赖任何 Android API，便于脱离 Android 环境做单元测试。
 */
package org.opendroneid.android.bluetooth;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Gb46750Parser {

    /** 数据类型固定取值（表1）。 */
    public static final int DATA_TYPE = 255; // 0xFF

    /** 21 个数据项的字节长度（索引 1..21，索引 0 未用），依据 §5.2.4 表3。 */
    public static final int[] ITEM_LENGTH = {
            0, 20, 8, 1, 1, 1, 8, 2, 8, 2, 2, 2, 1, 2, 2, 1, 1, 1, 1, 1, 6, 1
    };

    /** 数据项名称（索引 1..21）。 */
    public static final String[] ITEM_NAME = {
            "", "唯一产品识别码", "实名登记标志", "民用无人驾驶航空器系统运行类别",
            "民用无人驾驶航空器分类", "民用无人驾驶航空器遥控站位置类型",
            "民用无人驾驶航空器遥控站位置", "民用无人驾驶航空器遥控站高度",
            "民用无人驾驶航空器位置", "航迹角", "地速", "相对高度", "垂直速度",
            "大地高度", "气压高度", "运行状态", "坐标系类型", "水平精度",
            "垂直精度", "速度精度", "时间戳", "时间戳精度"
    };

    /** 必选/可选（索引 1..21）。 */
    public static final boolean[] ITEM_MANDATORY = {
            false, true, true, false, true, true, true, true, true, true, true,
            false, false, true, true, true, true, true, true, true, true, true
    };

    // 枚举描述表
    private static final String[] ENUM_003 = {"未定义", "开放类", "特定类", "审定类"}; // 运行类别
    private static final String[] ENUM_004 = {"微型", "轻型", "小型", "中型", "大型"};   // 分类
    private static final String[] ENUM_005 = {"起飞点位置", "遥控站位置"};              // 飞手位置类型
    private static final String[] ENUM_015 = {"未报告", "地面", "空中", "紧急状态",
            "运行识别发送功能失效(非紧急)", "运行识别发送功能失效(紧急)"};            // 运行状态
    private static final String[] ENUM_016 = {"WGS-84", "CGCS2000"};                  // 坐标系类型
    private static final String[] ENUM_017 = {                                       // 水平精度
            "≥18.52 km 或未知", "<18.52 km", "<7.41 km", "<3.70 km", "<1852 m",
            "<926 m", "<556 m", "<185 m", "<92.6 m", "<30 m", "<10 m", "<3 m",
            "<1 m"};
    private static final String[] ENUM_018 = {                                       // 垂直精度
            "≥150 m 或未知", "<150 m", "<45 m", "<25 m", "<10 m", "<3 m", "<1 m"};
    private static final String[] ENUM_019 = {                                       // 速度精度
            "≥10 m/s 或未知", "<10 m/s", "<3 m/s", "<1 m/s", "<0.3 m/s"};
    private static final String[] ENUM_021 = {                                       // 时间戳精度
            ">0.5 s 或未知", "≤0.5 s", "≤0.4 s", "≤0.3 s", "≤0.2 s", "≤0.1 s",
            "≤50 ms", "≤20 ms", "≤10 ms"};

    /** 单条已解码的数据项。 */
    public static class DecodedItem {
        public final int index;       // 1..21
        public final String name;     // 名称
        public final boolean mandatory;
        public final String value;    // 解码后的可读值
        public final byte[] raw;      // 原始字节

        DecodedItem(int index, String value, byte[] raw) {
            this.index = index;
            this.name = ITEM_NAME[index];
            this.mandatory = ITEM_MANDATORY[index];
            this.value = value;
            this.raw = raw;
        }
    }

    /** 一次解析出的完整国标报文。 */
    public static class Gb46750Message {
        public final int dataType;
        public final int version;           // V1.X 中的 X（0..31）
        public final int dataLength;
        public final int[] idBytes;         // 数据标识原始字节
        public final boolean[] present = new boolean[22]; // 索引 1..21
        public final List<DecodedItem> items = new ArrayList<>();

        public Gb46750Message(int dataType, int version, int dataLength, int[] idBytes) {
            this.dataType = dataType;
            this.version = version;
            this.dataLength = dataLength;
            this.idBytes = idBytes;
        }

        /** 是否包含所有必选(M)数据项。 */
        public boolean hasAllMandatory() {
            for (int i = 1; i <= 21; i++) {
                if (ITEM_MANDATORY[i] && !present[i])
                    return false;
            }
            return true;
        }

        /** 生成多行解码摘要（用于"信息"详情页的国标区块与日志）。 */
        public String toSummary() {
            if (items.isEmpty())
                return String.format(Locale.US,
                        "GB 46750-2025 报文 版本 V1.%d 数据长度 %d 字节\n（数据内容项为空）",
                        version, dataLength);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(Locale.US,
                    "GB 46750-2025 报文 版本 V1.%d 数据长度 %d 字节\n", version, dataLength));
            for (DecodedItem it : items) {
                sb.append(String.format(Locale.US, "  [%02d]%s(%s): %s\n",
                        it.index, it.name, it.mandatory ? "必选" : "可选", it.value));
            }
            return sb.toString();
        }
    }

    /**
     * 检测从 offset 开始的字节是否可能是 GB 46750-2025 报文（与 ASTM F3411 区分）。
     * 依据：数据类型==0xFF，且版本号高 3 位固定为 001（即 (ver & 0xE0)==0x20）。
     * ASTM F3411 的 MESSAGE_PACK 头虽也是 0xFF，但其版本号高 3 位不可能为 001，
     * 因此可区分。
     */
    public static boolean detect(byte[] data, int offset) {
        if (data == null || offset < 0 || offset + 3 >= data.length)
            return false;
        int dataType = data[offset] & 0xFF;
        int versionByte = data[offset + 1] & 0xFF;
        return dataType == DATA_TYPE && (versionByte & 0xE0) == 0x20;
    }

    /** 解析 GB 46750-2025 报文；失败返回 null。 */
    public static Gb46750Message parse(byte[] data, int offset) {
        if (!detect(data, offset))
            return null;
        int dataType = data[offset] & 0xFF;
        int versionByte = data[offset + 1] & 0xFF;
        int version = versionByte & 0x1F; // 低 5 位（标准正文写为 0~63，按 5 位取，超过则截断）
        int dataLength = data[offset + 2] & 0xFF;
        if (dataLength < 1 || dataLength > 200)
            return null;

        // 解析数据标识（带扩展标志位 0x01）
        int p = offset + 3;
        ArrayList<Integer> idList = new ArrayList<>();
        int guard = 0;
        while (p < data.length && guard++ < 8) {
            int b = data[p] & 0xFF;
            idList.add(b);
            p++;
            if ((b & 0x01) == 0) // 扩展标志位为 0 → 数据标识结束
                break;
        }
        int numIdBytes = idList.size();
        int[] idBytes = new int[numIdBytes];
        for (int i = 0; i < numIdBytes; i++)
            idBytes[i] = idList.get(i);

        Gb46750Message msg = new Gb46750Message(dataType, version, dataLength, idBytes);

        // 重新扫描数据标识建立 present 表
        for (int i = 0; i < numIdBytes; i++) {
            int b = idBytes[i];
            int base = i * 7 + 1;
            int idx = 0;
            for (int bit = 0x80; bit >= 0x02; bit >>= 1, idx++) {
                if ((b & bit) != 0)
                    msg.present[base + idx] = true;
            }
        }

        // 数据内容项：从 p 开始，长度 dataLength
        if (p + dataLength > data.length)
            return null;
        ByteBuffer bb = ByteBuffer.wrap(data, p, dataLength).order(ByteOrder.LITTLE_ENDIAN);

        for (int item = 1; item <= 21; item++) {
            if (!msg.present[item])
                continue;
            int len = ITEM_LENGTH[item];
            if (bb.remaining() < len)
                return null;
            byte[] raw = new byte[len];
            bb.get(raw);
            msg.items.add(decodeItem(item, raw, bb.order()));
        }
        return msg;
    }

    private static DecodedItem decodeItem(int item, byte[] raw, ByteOrder order) {
        switch (item) {
            case 1:  // 唯一产品识别码 20B ASCII
            case 2:  // 实名登记标志 8B ASCII
                return new DecodedItem(item, ascii(raw), raw);
            case 3:  // 运行类别 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_003), raw);
            case 4:  // 分类 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_004), raw);
            case 5:  // 飞手位置类型 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_005), raw);
            case 6:  // 飞手位置 8B 小端序 lat|lon ×1e-7
            case 8:  // 无人机位置 8B 小端序 lat|lon ×1e-7
                return new DecodedItem(item, latLon(raw), raw);
            case 7:  // 飞手高度 2B 小端序 (val+1000)×2
            case 13: // 大地高度 2B
            case 14: // 气压高度 2B
                return new DecodedItem(item, signedAlt(raw), raw);
            case 11: // 相对高度 2B 小端序 (val+9000)×2
                return new DecodedItem(item, String.format(Locale.US, "%.1f m", leU16(raw) / 2.0 - 9000.0), raw);
            case 9:  // 航迹角 2B 小端序 ×10
                return new DecodedItem(item, leU16(raw) == 0xFFFF ? "未知/不可用"
                        : String.format(Locale.US, "%.1f°", leU16(raw) / 10.0), raw);
            case 10: // 地速 2B 小端序 ×10
                return new DecodedItem(item, leU16(raw) == 0xFFFF ? "未知/不可用"
                        : String.format(Locale.US, "%.1f m/s", leU16(raw) / 10.0), raw);
            case 12: { // 垂直速度 1B bit7 标志 + (val&0x7F)×2
                int r = raw[0] & 0xFF;
                if (r == 0xFF) return new DecodedItem(item, "未知/不可用", raw);
                double v = (r & 0x7F) * 0.5;
                return new DecodedItem(item, String.format(Locale.US, "%s%.1f m/s",
                        (r & 0x80) != 0 ? "-" : "", v), raw);
            }
            case 15: // 运行状态 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_015), raw);
            case 16: // 坐标系类型 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_016), raw);
            case 17: // 水平精度 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_017), raw);
            case 18: // 垂直精度 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_018), raw);
            case 19: // 速度精度 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_019), raw);
            case 20: { // 时间戳 6B 小端序 Unix ms
                long ms = leU48(raw);
                return new DecodedItem(item, String.format(Locale.US, "%d ms", ms), raw);
            }
            case 21: // 时间戳精度 1B
                return new DecodedItem(item, enumText(raw[0] & 0xFF, ENUM_021), raw);
            default:
                return new DecodedItem(item, "—", raw);
        }
    }

    private static String ascii(byte[] raw) {
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            int c = b & 0xFF;
            if (c == 0) break; // 以空字符 NULL 填充结尾
            sb.append((c >= 0x20 && c < 0x7F) ? (char) c : '.');
        }
        String s = sb.toString();
        return s.isEmpty() ? "（空）" : s;
    }

    private static String enumText(int code, String[] table) {
        if (code < table.length)
            return String.format(Locale.US, "%d=%s", code, table[code]);
        return String.format(Locale.US, "%d=预留", code);
    }

    private static String latLon(byte[] raw) {
        int lat = leI32(raw, 0);
        int lon = leI32(raw, 4);
        if (lat == 0xFFFFFFFF || lon == 0xFFFFFFFF)
            return "未知/不可用";
        return String.format(Locale.US, "纬度 %.7f°, 经度 %.7f°", lat * 1e-7, lon * 1e-7);
    }

    private static String signedAlt(byte[] raw) {
        int v = leU16(raw);
        if (v == 0) return "未知/不可用";
        return String.format(Locale.US, "%.1f m", v / 2.0 - 1000.0);
    }

    private static int leU16(byte[] raw) {
        return (raw[0] & 0xFF) | ((raw[1] & 0xFF) << 8);
    }

    private static int leI32(byte[] raw, int off) {
        return (raw[off] & 0xFF) | ((raw[off + 1] & 0xFF) << 8)
                | ((raw[off + 2] & 0xFF) << 16) | ((raw[off + 3] & 0xFF) << 24);
    }

    private static long leU48(byte[] raw) {
        long v = 0;
        for (int i = 0; i < 6; i++)
            v |= ((long) (raw[i] & 0xFF)) << (8 * i);
        return v;
    }

    // ---- 离线自测：构造一个合成国标报文并解析，验证 21 项解码 ----
    public static void main(String[] args) {
        // 构造：数据类型=0xFF, 版本 V1.0, 数据长度=待定, 数据标识 3 字节(全部必选+若干可选置位), 数据内容项
        // 这里只放一个最小用例：唯一产品识别码(001) + 实名登记标志(002) + 无人机位置(008)
        byte[] content = new byte[20 + 8 + 8];
        // 001 唯一产品识别码 "GB2025TESTUAS12345" (20)
        String uas = "GB2025TESTUAS12345";
        System.arraycopy(uas.getBytes(StandardCharsets.US_ASCII), 0, content, 0, uas.length());
        // 002 实名登记标志 "REG12345" (8)
        String reg = "REG12345";
        System.arraycopy(reg.getBytes(StandardCharsets.US_ASCII), 0, content, 20, reg.length());
        // 008 无人机位置 8B: lat=39.9°, lon=116.4° ×1e7, 小端序
        int lat = (int) Math.round(39.9 * 1e7);
        int lon = (int) Math.round(116.4 * 1e7);
        putLeI32(content, 28, lat);
        putLeI32(content, 32, lon);

        int dataLength = content.length;
        // 数据标识：第1字节 0x80(001) | 0x40(002) | 0x01(扩展) = 0xC1；
        //          第2字节 0x80(008) | 0x01(扩展) = 0x81；第3字节 0x00（扩展位为0，结束）
        // 扩展标志位(0x01)表示"后续字节仍属数据标识"，因此前两个字节都必须置位。
        byte[] idBytes = new byte[]{(byte) 0xC1, (byte) 0x81, 0x00};

        byte[] packet = new byte[3 + idBytes.length + dataLength];
        packet[0] = (byte) 0xFF;        // 数据类型
        packet[1] = (byte) 0x20;        // 版本号：高3位001, 低5位000 → 0x20
        packet[2] = (byte) dataLength;   // 数据长度
        System.arraycopy(idBytes, 0, packet, 3, idBytes.length);
        System.arraycopy(content, 0, packet, 3 + idBytes.length, dataLength);

        Gb46750Message m = parse(packet, 0);
        if (m == null) {
            System.out.println("PARSE FAILED");
            System.exit(1);
        }
        System.out.println("版本 V1." + m.version + " 数据长度 " + m.dataLength);
        for (DecodedItem it : m.items) {
            System.out.println("  [" + String.format("%02d", it.index) + "]" + it.name + " = " + it.value);
        }
        // 断言
        boolean ok = m.present[1] && m.present[2] && m.present[8]
                && !m.present[3] && m.items.size() == 3
                && m.items.get(0).value.equals("GB2025TESTUAS12345")
                && m.items.get(1).value.equals("REG12345")
                && m.items.get(2).value.contains("39.9")
                && m.items.get(2).value.contains("116.4");
        System.out.println(ok ? "SELF-TEST PASS" : "SELF-TEST FAIL");
        System.exit(ok ? 0 : 1);
    }

    private static void putLeI32(byte[] a, int off, int v) {
        a[off] = (byte) (v & 0xFF);
        a[off + 1] = (byte) ((v >> 8) & 0xFF);
        a[off + 2] = (byte) ((v >> 16) & 0xFF);
        a[off + 3] = (byte) ((v >> 24) & 0xFF);
    }
}
