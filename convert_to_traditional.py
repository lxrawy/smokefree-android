#!/usr/bin/env python3
"""Convert simplified Chinese strings.xml to Traditional Chinese (TW & HK variants) using OpenCC."""
from opencc import OpenCC

SRC = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/src/main/res/values-zh/strings.xml"

# Read simplified Chinese source
with open(SRC, "r", encoding="utf-8") as f:
    simplified = f.read()

# TW: s2tw (Simplified to Traditional Taiwanese)
cc_tw = OpenCC("s2tw")
tw_content = cc_tw.convert(simplified)

# HK: s2hk (Simplified to Traditional Hong Kong)
cc_hk = OpenCC("s2hk")
hk_content = cc_hk.convert(simplified)

# Domain-specific term adjustments for TW
TW_TERMS = [
    ("吸煙", "吸菸"),
    ("戒煙", "戒菸"),
    ("香煙", "香菸"),
    ("抽煙", "抽菸"),
    ("煙錢", "菸錢"),
    ("一根煙", "一根菸"),
    ("支煙", "支菸"),
    ("不煙", "不菸"),
    ("沒煙", "沒菸"),
    ("未煙", "未菸"),
    ("已煙", "已菸"),
    ("煙", "菸"),
    ("登錄", "登入"),
    ("註冊", "註冊"),
    ("視頻", "影片"),
    ("網絡", "網路"),
    ("軟件", "軟體"),
    ("數據", "資料"),
    ("導出", "匯出"),
    ("導入", "匯入"),
    ("保存", "儲存"),
    ("設置", "設定"),
    ("微信", "WeChat"),
    ("反饋", "回饋"),
    ("默認", "預設"),
    ("信息", "資訊"),
    ("質量", "品質"),
    ("屏幕", "螢幕"),
    ("通過", "透過"),
    ("啟用", "啟用"),
    ("啟動", "啟動"),
    ("開啟", "開啟"),
    ("設備", "裝置"),
    ("智能", "智慧"),
    ("短信", "簡訊"),
    ("打印", "列印"),
]

for old, new in TW_TERMS:
    tw_content = tw_content.replace(old, new)

# HK-specific term adjustments
HK_TERMS = [
    ("吸煙", "吸菸"),
    ("戒煙", "戒菸"),
    ("香煙", "香菸"),
    ("抽煙", "抽菸"),
    ("煙錢", "菸錢"),
    ("一根煙", "一根菸"),
    ("支煙", "支菸"),
    ("不煙", "不菸"),
    ("沒煙", "沒菸"),
    ("未煙", "未菸"),
    ("已煙", "已菸"),
    ("煙", "菸"),
    ("登錄", "登入"),
    ("視頻", "影片"),
    ("網絡", "網絡"),  # HK keeps 網絡
    ("軟件", "軟件"),  # HK keeps 軟件
    ("數據", "資料"),
    ("導出", "匯出"),
    ("導入", "匯入"),
    ("保存", "儲存"),
    ("設置", "設定"),
    ("微信", "WeChat"),
    ("反饋", "回饋"),
    ("默認", "預設"),
    ("信息", "資訊"),
    ("質量", "品質"),
    ("屏幕", "螢幕"),
    ("通過", "透過"),
    ("啟用", "啟用"),
    ("啟動", "啟動"),
    ("開啟", "開啟"),
    ("設備", "裝置"),
    ("智能", "智能"),
    ("短信", "簡訊"),
    ("打印", "列印"),
]

for old, new in HK_TERMS:
    hk_content = hk_content.replace(old, new)

# Also adjust currency symbols for TW and HK
tw_content = tw_content.replace("¥", "NT$")
hk_content = hk_content.replace("¥", "HK$")

# Write TW
tw_path = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/src/main/res/values-zh-rTW/strings.xml"
with open(tw_path, "w", encoding="utf-8") as f:
    f.write(tw_content)
print(f"TW written: {tw_path}")

# Write HK
hk_path = "/Users/cole/WorkBuddy/Claw/android-smokefree/app/src/main/res/values-zh-rHK/strings.xml"
with open(hk_path, "w", encoding="utf-8") as f:
    f.write(hk_content)
print(f"HK written: {hk_path}")

# Show a few sample lines for verification
print("\n=== Sample comparison (first 20 lines) ===")
lines_tw = tw_content.split("\n")
lines_hk = hk_content.split("\n")
for i, (lt, lh) in enumerate(zip(lines_tw[:20], lines_hk[:20])):
    print(f"TW: {lt}")
    print(f"HK: {lh}")
    print()
