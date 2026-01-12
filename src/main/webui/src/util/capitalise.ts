export const capitalise = (str: string) => {
    return str.split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

export const skewToCapText = (str: string) => {
    return str.toLowerCase().replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}