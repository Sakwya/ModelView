function main() {
	let scene = new THREE.Scene();
	let camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

	let renderer = new THREE.WebGLRenderer();
	renderer.setSize(window.innerWidth, window.innerHeight);
	document.body.appendChild(renderer.domElement);

	// 添加一个简单的立方体
	let geometry = new THREE.BoxGeometry();
	let material = new THREE.MeshBasicMaterial({
		color: 0x00ff00
	});
	let cube = new THREE.Mesh(geometry, material);
	scene.add(cube);

	camera.position.z = 5;

	var animationRunning = true
	var rotation = {
		'x': 0,
		'y': 0,
		'z': 0
	}
	const animate = function() {
		if (!animationRunning) {
			return
		}
		requestAnimationFrame(animate);
		cube.rotation.x += 0.1 * (rotation.x - cube.rotation.x);
		cube.rotation.y += 0.1 * (rotation.y - cube.rotation.y);
		cube.rotation.z += 0.1 * (rotation.z - cube.rotation.z);

		renderer.render(scene, camera);
	};
	const stopRender = function() {
		animationRunning = false
	}
	const startRender = function() {
		if (animationRunning == true) {
			console.log("Render has been started.")
			return false
		}
		animationRunning = true
		requestAnimationFrame(animate)

	}
	const setRotation = function(x, y, z) {
		rotation.x = x
		rotation.y = y
		rotation.z = z
	}
	animate();
	return {
		startRender,
		stopRender,
		setRotation,
	}
}
const render = main()