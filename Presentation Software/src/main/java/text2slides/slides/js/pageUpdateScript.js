//<script> 
document.addEventListener('DOMContentLoaded', function() {
	var intervalId = setInterval(function() { changePage(0);}, 0);
    var intervalId = setInterval(function() { 				window.location.search = `?page=${currentPage}`;

					window.location.reload();}, 2000);

    document.getElementById('toggleButton').addEventListener('click', function() {
        if (intervalId) {
            clearInterval(intervalId);
            intervalId = null;
        } else {
            intervalId = setInterval(function() {
				window.location.search = `?page=${currentPage}`;
                window.location.reload();
            }, 2000);
        }
    });
});
//</script>
//</body> 
//</html>  