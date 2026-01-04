export const formatCurrency = (value: number, locale = 'en-NZ', currency = 'NZD'): string => {
    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currency,
    }).format(value);
};