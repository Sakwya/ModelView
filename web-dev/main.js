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
	let scene = new THREE.Scene();
	window.scene = scene
	let camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
	// const camera = new THREE.OrthographicCamera(-window.innerWidth*0.5 / window.innerHeight,window.innerWidth*0.5 / window.innerHeight,1,-0, 1, 1000);

	let renderer = new THREE.WebGLRenderer();
	renderer.setSize(window.innerWidth, window.innerHeight);
	document.body.appendChild(renderer.domElement);


	const current = {}
	window.current = current
	current.model = new THREE.Object3D();
	let controls = new THREE.OrbitControls(camera, renderer.domElement);
	scene.background = new THREE.Color(0x404040); // 灰色背景

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
		group.position.x = size.x / maxDimension * -0.5
		group.position.z = size.z / maxDimension * -0.5
		group.position.y = size.y / maxDimension * 0.5
		group.add(model);
		model.position.x = size.x / maxDimension * 0.5
		model.position.z = size.z / maxDimension * 0.5
		model.position.y = size.y / maxDimension * -0.5

		group.position.sub(center)
		// const helper = new THREE.Box3Helper(box, 0xffff00);
		// scene.add(helper);

		// group.position.copy(center);
		// console.log(group)
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
	const animate = function() {
		if (!animationRunning) {
			return
		}
		requestAnimationFrame(animate);
		current.model.rotation.x += 0.1 * (rotation.x - current.model.rotation.x);
		current.model.rotation.y += 0.1 * (rotation.y - current.model.rotation.y);
		current.model.rotation.z += 0.1 * (rotation.z - current.model.rotation.z);

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
	const setRotation = function(x, y, z) {
		rotation.x += (x - rotation.x) % (2 * Math.PI)
		rotation.y += (y - rotation.y) % (2 * Math.PI)
		rotation.z += (z - rotation.z) % (2 * Math.PI)
	}
	const setRawRotation = function(x, y, z) {
		rotation.x = x
		rotation.y = y
		rotation.z = z
	}
	// loadModel("./assets/blue_archivekasumizawa_miyu/scene.gltf").then(animate())
	loadModel("./assets/city.glb")
	
	animate()
	return {
		startRender,
		stopRender,
		setPosition,
		setRotation,
		setRawRotation,
		light_intensity,
		loadModel
	}
}
const render = main()
render.loadModel("./assets/just_a_girl.glb")