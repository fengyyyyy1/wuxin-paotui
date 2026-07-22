export const MERCHANT_AUDIT = { pending: 0, approved: 1, rejected: 2 } as const;
export const MERCHANT_STATUS = { disabled: 0, enabled: 1 } as const;
export const BUSINESS_STATUS = { closed: 0, open: 1 } as const;
export const ORDER_STATUS = { waiting: 0, accepted: 1, delivering: 2, waitingConfirm: 3, completed: 4, cancelled: 5, preparing: 6, waitingRider: 7, waitingRefund: 8 } as const;
