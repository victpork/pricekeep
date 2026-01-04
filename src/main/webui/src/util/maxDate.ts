export const maxDate = (dates: Date[]) => {
    return new Date(Math.max(...dates.map(d => d.getTime())))
}