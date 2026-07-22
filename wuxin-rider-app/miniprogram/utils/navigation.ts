export function copyText(value: string | null | undefined): void {
  if (!value) { wx.showToast({ title: '暂无可复制内容', icon: 'none' }); return; }
  wx.setClipboardData({ data: value });
}

export function callPhone(value: string | null | undefined): void {
  if (!value) { wx.showToast({ title: '暂无联系电话', icon: 'none' }); return; }
  wx.makePhoneCall({ phoneNumber: value });
}

export function openLocation(latitude: number | null | undefined, longitude: number | null | undefined, name: string, address: string): void {
  if (latitude == null || longitude == null) { wx.showToast({ title: '该地址暂无定位信息', icon: 'none' }); return; }
  wx.openLocation({ latitude: Number(latitude), longitude: Number(longitude), name, address, scale: 16 });
}
