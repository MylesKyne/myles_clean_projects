<button id="toggleButton">Edit mod ON. Press space or click this button to activate presentation mod. Use ArrowLeft and ArrowRight to change slides. ArrowDown turns on dark mode.</button>

//<script>
    function changeBackground() {
        var currentElement = document.getElementById('page' + currentPage);
        if (currentElement) {
            const layout = currentElement.getAttribute('layout');
            const imageUrl = currentElement.getAttribute('background');
            if(imageUrl){
                document.body.style.textAlign = layout;
                document.body.style.backgroundImage = `url('${imageUrl}')`;
            }
            else{
                document.body.style.textAlign = '';
                document.body.style.backgroundImage = '';
            }
        }
    }

    window.onload = changeBackground;
//</script>
//<script>
    var currentPage = //CURRENTPAGE;
    var totalPages = //MAXPAGENUM;

    function changePage(step) {
        currentPage += step;

        if (currentPage < 2) {
            currentPage = 2;
        } else if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        var pages = document.getElementsByClassName('page');
        for (var i = 0; i < pages.length; i++) {
            pages[i].style.display = 'none';
        }
        document.getElementById('page' + currentPage).style.display = 'block'; 
        
        if(step != 0)
        {
			const currentUrl = new URL(window.location);
    		currentUrl.searchParams.set('page', currentPage);
    		history.pushState({}, '', currentUrl);
		}
          
        changeBackground();

    }
    function slidesOverview() {
        var pages = document.getElementsByClassName('page');
        for (var i = 0; i < pages.length; i++) {
            pages[i].style.display = 'block';
        }
        document.body.style.textAlign = '';
        document.body.style.backgroundImage = '';
    }
    function toggleFullScreen() {
		  if (document.documentElement.webkitRequestFullscreen)
		  {
			 document.documentElement.webkitRequestFullscreen();
		  }

    }
    function toggleDarkMode() {
        document.body.classList.toggle('dark-mode');
    }
    
    
  document.addEventListener('keydown', function(event) {

  if (event.key === "ArrowRight") {
    changePage(1);
  }
  if (event.key === "ArrowLeft") {
    changePage(-1);
  }
  if (event.key === "ArrowUP") {
    toggleFullScreen();
  }
  if (event.key === "ArrowDown") {
    toggleDarkMode();
  }
  if (event.code === "Space") {
	const button = document.getElementById('toggleButton');
	
	if(button.style.display === "none")
	{
		button.style.display = "inline-block";
		button.textContent = 'Edit mod ON. Press space or click this button to activate presentation mod. Use ArrowLeft and ArrowRight to change slides. ArrowDown turns on dark mode.';

	}
	else
	{
		button.style.display = "none";
	}
	button.click();
  }
  
});
    
//</script>
