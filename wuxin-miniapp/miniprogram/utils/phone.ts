export function maskPhone(phone?: string | null): string {
  if (!phone) {
    return '未绑定';
  }

  if (!/^1\d{10}$/.test(phone)) {
    return phone;
  }

  return `${phone.slice(0, 3)}****${phone.slice(7)}`;
}
