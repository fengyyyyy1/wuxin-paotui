export const money = (value: number | null | undefined): string => Number(value || 0).toFixed(2);
export const dateTime = (value: string | null | undefined): string => value ? value.replace('T', ' ').slice(0, 16) : '--';
export const displayName = (value: string | null | undefined, fallback = '五鑫商家'): string => value?.trim() || fallback;
export function localDayRange(): { startTime: string; endTime: string } {
  const now = new Date(); const start = new Date(now.getFullYear(), now.getMonth(), now.getDate()); const end = new Date(start.getTime() + 86400000);
  const toLocal = (date: Date): string => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}T${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`;
  return { startTime: toLocal(start), endTime: toLocal(end) };
}
