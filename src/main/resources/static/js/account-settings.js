// Account Settings JS
// Handles dark theme toggle (optional for future dynamic theme switching)

document.addEventListener('DOMContentLoaded', function () {
    const darkThemeCheckbox = document.querySelector('input[name="darkTheme"]');
    const darkThemeCss = document.getElementById('dark-theme-css');

    if (darkThemeCheckbox) {
        darkThemeCheckbox.addEventListener('change', function () {
            if (this.checked) {
                document.body.classList.add('dark-theme');
                if (darkThemeCss) darkThemeCss.removeAttribute('disabled');
            } else {
                document.body.classList.remove('dark-theme');
                if (darkThemeCss) darkThemeCss.setAttribute('disabled', '');
            }
        });
    }
});
