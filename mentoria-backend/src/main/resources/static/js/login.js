document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const dni = document.getElementById('dni').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/usuarios/dni/' + dni);
        if (!response.ok) {
            throw new Error('Usuario no encontrado');
        }

        const user = await response.json();
        // Aquí deberías implementar la lógica de autenticación real
        // Por ahora, solo redirigimos si el usuario existe
        localStorage.setItem('user', JSON.stringify(user));
        window.location.href = 'dashboard.html';
    } catch (error) {
        alert('Error de autenticación: ' + error.message);
    }
}); 