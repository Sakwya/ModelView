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
	let camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);
	// const camera = new THREE.OrthographicCamera(-window.innerWidth*0.5 / window.innerHeight,window.innerWidth*0.5 / window.innerHeight,1,-0, 1, 1000);

	let renderer = new THREE.WebGLRenderer();
	renderer.setSize(window.innerWidth, window.innerHeight);
	document.body.appendChild(renderer.domElement);


	const current = {}
	window.current = current
	current.model = new THREE.Object3D();
	let controls = new THREE.OrbitControls(camera, renderer.domElement);
	const axesHelper = new THREE.AxesHelper(5);
	scene.add(axesHelper);

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
		// group.position.x = size.x / maxDimension * -0.5
		// group.position.z = size.z / maxDimension * -0.5
		// group.position.y = size.y / maxDimension * 0.5
		model.position.sub(center)
		group.add(model);
		// model.position.x = size.x / maxDimension * 0.5
		// model.position.z = size.z / maxDimension * 0.5
		// model.position.y = size.y / maxDimension * -0.5

		// group.position.sub(center)

		const geometry = new THREE.BoxGeometry(0.025, 0.025, 0.025);
		const material = new THREE.MeshBasicMaterial({
			color: 0x00ff00
		});
		const cube = new THREE.Mesh(geometry, material);
		scene.add(cube);
		cube.position.copy(group.position)

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
		setTimeout(requestAnimationFrame(animate), 500)
		// requestAnimationFrame(animate);
		let temp = (rotation.x - current.model.rotation.x) % PI2
		if (temp < 0) temp += PI2
		if (temp < Math.PI) {
			current.model.rotation.x += 0.05 * temp;
		} else {
			current.model.rotation.x -= 0.05 * (PI2 - temp);
		}

		temp = (rotation.y - current.model.rotation.y) % PI2
		if (temp < 0) temp += PI2
		if (temp < Math.PI) {
			current.model.rotation.y += 0.05 * temp;
		} else {
			current.model.rotation.y -= 0.05 * (PI2 - temp);
		}

		temp = (rotation.z - current.model.rotation.z) % PI2
		if (temp < 0) temp += PI2
		if (temp < Math.PI) {
			current.model.rotation.z += 0.05 * temp;
		} else {
			current.model.rotation.z -= 0.05 * (PI2 - temp);
		}

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

	function modPi(x) {
		let temp = x % (PI2)
		if (temp < 0) return PI2 + temp
		return temp
	}
	const setRotation = function(x, y, z) {
		rotation.x = modPi(x)
		rotation.y = modPi(y)
		rotation.z = modPi(z)
	}
	window.r = setRotation
	const setRawRotation = function(x, y, z) {
		rotation.x = x
		rotation.y = y
		rotation.z = z
	}
	// loadModel("./assets/blue_archivekasumizawa_miyu/scene.gltf").then(animate())
	// loadModel("./assets/city.glb")
	const setQuaternion = function(q0, q1, q2, q3) {
		current.model.quaternion.copy(new THREE.Quaternion(q1, q2, q3, q0));
	}




	navigator.mediaDevices.getUserMedia({
		video: true
	}).then(function(stream) {
		var video = document.createElement('video');
		video.srcObject = stream;
		video.play();

		// 创建视频纹理
		var videoTexture = new THREE.VideoTexture(video);
		videoTexture.minFilter = THREE.LinearFilter;
		videoTexture.magFilter = THREE.LinearFilter;
		videoTexture.format = THREE.RGBFormat;

		// 创建一个全屏四边形
		var geometry = new THREE.PlaneGeometry(5 * camera.aspect, 5);
		var material = new THREE.MeshBasicMaterial({
			map: videoTexture
		});
		var mesh = new THREE.Mesh(geometry, material);

		// // 调整四边形的位置和缩放
		mesh.position.set(0, 0, -1);
		// mesh.scale.set(camera.aspect, 1, 1);

		// 添加四边形到场景
		scene.add(mesh);

	}).catch(function(error) {
		console.error('Error accessing the camera:', error);
	});

	animate()
	return {
		startRender,
		stopRender,
		setPosition,
		setRotation,
		setRawRotation,
		setQuaternion,
		light_intensity,
		loadModel
	}
}
const render = main()
render.loadModel("./assets/just_a_girl.glb")