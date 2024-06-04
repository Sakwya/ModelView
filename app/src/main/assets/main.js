const light_setup = () => {
	const light_array = []
	let ambientLight = new THREE.AmbientLight(0x404040, 2); // 白色环境光
	let directionalLight0 = new THREE.DirectionalLight(0xffffff, 1);
	directionalLight0.position.set(1, 1, 1).normalize();
	let directionalLight1 = new THREE.DirectionalLight(0xffffff, 1);
	directionalLight1.position.set(-1, 1, 1).normalize();
	let directionalLight2 = new THREE.DirectionalLight(0xffffff, 1);
	directionalLight2.position.set(1, 1, -1).normalize();
	let directionalLight3 = new THREE.DirectionalLight(0xffffff, 1);
	directionalLight3.position.set(-1, 1, -1).normalize();
	const hemisphereLight = new THREE.HemisphereLight(0xffffbb, 0x080820, 1);
	light_array.push(ambientLight)
	light_array.push(directionalLight0)
	light_array.push(directionalLight1)
	light_array.push(directionalLight2)
	light_array.push(directionalLight3)
	light_array.push(hemisphereLight)
	return light_array
}

const main = () => {
	const PI2 = 2 * Math.PI
	let scene = new THREE.Scene();
	window.scene = scene
	const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
	// const camera = new THREE.OrthographicCamera(-window.innerWidth*0.5 / window.innerHeight,window.innerWidth*0.5 / window.innerHeight,1,-0, 1, 1000);

	const renderer = new THREE.WebGLRenderer({ alpha: true ,antialias: true});
	renderer.setSize(window.innerWidth, window.innerHeight);
	document.body.appendChild(renderer.domElement);
    renderer.setClearColor(0x00000000, 0);

	const current = {}
	window.current = current
	current.model = new THREE.Object3D();
	let controls = new THREE.OrbitControls(camera, renderer.domElement);

	const light_array = light_setup()
	for (const light of light_array) {
		scene.add(light)
	}
	const light_intensity = (intensity) => {
		for (const light of light_array) {
			light.intensity = intensity
		}
	}

	function calculateScaleToFit(model) {
		var box = new THREE.Box3().setFromObject(model);

		const size = new THREE.Vector3();
		let center = new THREE.Vector3();
		box.getSize(size);
		const maxDimension = Math.max(size.x, size.y, size.z);
		center.x = size.x / 2;
		center.y = size.y / 2;
		center.z = size.z / 2;
		window.x = size.x / maxDimension
		window.y = size.y / maxDimension
		window.z = size.z / maxDimension
		window.model = model
		// model.position.copy(center);
		const scale = 1 / maxDimension;
		model.scale.set(scale, scale, scale);

		box = new THREE.Box3().setFromObject(model);

		box.getCenter(center)

		const group = new THREE.Group();
		model.position.sub(center)
		group.add(model);

//		const geometry = new THREE.BoxGeometry(0.025, 0.025, 0.025);
//		const material = new THREE.MeshBasicMaterial({
//			color: 0x00ff00
//		});
//		const cube = new THREE.Mesh(geometry, material);
//		scene.add(cube);
//		cube.position.copy(group.position)

		return group
	}

	// 加载模型的函数
	const loadModel = function(modelUrl) {
		scene.remove(current.model);

		let loader = new THREE.GLTFLoader();
		loader.load(modelUrl, function(gltf) {
			current.model = calculateScaleToFit(gltf.scene)
			scene.add(current.model);
		});
	}
	camera.position.z = 2;
	var animationRunning = true
	var rotation = {
		'x': 0,
		'y': 0,
		'z': 0
	}
	const targetQuaternion = new THREE.Quaternion();
	const currentQuaternion = new THREE.Quaternion();
	const animate = function() {
		if (!animationRunning) {
			return
		}
		setTimeout(requestAnimationFrame(animate), 100)
		currentQuaternion.slerp(targetQuaternion, 0.75);
		current.model.quaternion.copy(currentQuaternion);
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
		controls.update();
		requestAnimationFrame(animate)
	}
	const setPosition = function(x, y, z) {
		current.model.children[0].position.x = x
		current.model.children[0].position.y = y
		current.model.children[0].position.z = z
	}

	// loadModel("./assets/blue_archivekasumizawa_miyu/scene.gltf").then(animate())
	// loadModel("./assets/city.glb")
	const setQuaternion = function(q0, q1, q2, q3) {
		targetQuaternion.set(q1, q2, q3, q0);
	}

	animate()
	return {
		startRender,
		stopRender,
		setPosition,
		setQuaternion,
		light_intensity,
		loadModel
	}
}
const render = main()