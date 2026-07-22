export function money(value: number | null | undefined): string { return Number(value || 0).toFixed(2); }
export function dateTime(value: string | null | undefined): string { return value ? value.replace('T', ' ').slice(0, 16) : '--'; }
export function phone(value: string | null | undefined): string { return value || '未绑定'; }
export function displayName(name: string | null | undefined, fallback = '五鑫骑手'): string { return name?.trim() || fallback; }
