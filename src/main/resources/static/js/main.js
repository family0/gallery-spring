"use strict"
function openImg() {
    const url = event.target.src.replace('/image', '/open');
    fetch(url);
}
