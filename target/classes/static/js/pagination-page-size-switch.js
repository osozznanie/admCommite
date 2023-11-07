
    $(document).ready(function() {
        changePageAndSize();
    });
    function changePageAndSize() {
        $('#pageSizeSelect').change(function(evt) {
            window.location.replace(window.location.href.split('?')[0] +
                "?pageSize=" + this.value + "&page=1");
        });
    }

