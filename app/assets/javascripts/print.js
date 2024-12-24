const printlink = document.getElementById('print-button');

if(printlink != null && printlink != 'undefined' ) {

    printlink.addEventListener("click", function (e) {
        e.preventDefault();
        window.print();
    });
};