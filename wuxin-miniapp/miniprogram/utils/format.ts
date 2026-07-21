export function formatMoney(value: number | string | null | undefined): string {
  const amount = Number(value);
  if (!Number.isFinite(amount)) {
    return '0.00';
  }
  return amount.toFixed(2);
}
