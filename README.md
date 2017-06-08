# IoTrade
IoTrade - A marketplace for IoT

```
/**
 * @return Retorna os serviços sem analytics do algoritmo de matchmaking.
 */
exports.getSensorAlgorithm = (lat, lng, category) => 
	
	new Promise((resolve,reject) => {

		let sensors =[];
		let connect_chosen;
		let sensor_chosen;
		let high_rank = -1.0;

		const cypher = "MATCH (you:Profile) "
					+"MATCH (cn:Conection)-[:IS_NEAR]->(s:Sensor)-[:BELONGS_TO]->(c:Category {title: {category}}) "
					+"MATCH (s)-[sr:IS_IN]->(g:Group) MATCH (cn)-[cnr:IS_IN]->(g2:Group) "
					+"WHERE (sr.price + cnr.price) <= you.budget "
					+"RETURN cn, s, sr, cnr, g.title, g2.title ORDER BY cn.title";

		try{
			assert.isDefined(lat, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}
		
		try{
			assert.isDefined(lng, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}

		try{
			assert.isDefined(category, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}
						

		db.cypher({
		    query: cypher,
		    params: {
	            category: category											
		    },
		    lean: true
		}, (err, results) =>{
			try{
				assert.notExists(err, 'Sem erro!');
			}catch(err){
				console.log(err.message);
			}

			if (err) 
		    	reject({ status: 500, message: 'Internal Server Error !' });
		    else{
		    	let i, j;

		    	try{
					assert.isDefined(results, 'Vetor Existe!');
				}catch(err){
					console.log(err.message);
				}

		    	if(results && results.length > 0){

			    	for(i=0;i<results.length;i++){
			    		let j = i;
			    		let obj = results[i];
			    		let obj_next = results[j];
			    		let cn = obj['cn'];
			    		let cnr = obj['cnr'];

			    		cn.rank = parseFloat(cnr.sum)/parseFloat(cnr.qty);
			            cn.price = cnr.price;
			            cn.category = obj['g2.title'];

			    		let cn_next = obj_next['cn'];

			    		cn.array = [];
		
			    		while(cn.title == cn_next.title){
			    			let s = obj_next['s'];
			    			let sr = obj_next['sr'];
			    			s.rank = parseFloat(sr.sum)/parseFloat(sr.qty);
			            	s.price = sr.price;
			            	s.category = obj_next['g.title'];
			            	cn.array.push(s);

							j++;
							if(j < results.length){
								obj_next = results[j];
								cn_next = obj_next['cn'];
							}
							else{
								cn_next = String(-2);
							}
						}
						i=j-1;

						if(getDistanceFromLatLonInKm(lat, lng, cn.lat, cn.lng) < 1.5){
							cn.array.sort(function(a,b) {  
							    if (a.rank < b.rank)
				                    return 1;
				                else if (a.rank > b.rank)
				                    return -1;
				                else if (a.price < b.price)
				                    return -1;
				                else if (a.price > b.price)
				                	return 1;
				                else
				                	return 0;
							});
			            	sensors.push(cn);
						}
			    	}

			    	sensors.sort(function(a,b) {  
					    if (a.rank < b.rank)
		                    return 1;
		                else if (a.rank > b.rank)
		                    return -1;
		                else if (a.sgnl_net < b.sgnl_net)
		                    return 1;
		                else if (a.sgnl_net > b.sgnl_net)
		                    return -1;
		                else if (a.batery < b.batery)
		                    return 1;
		                else if (a.batery > b.batery)
		                	return -1;
		                else if (a.price < b.price)
		                    return -1;
		                else if (a.price > b.price)
		                	return 1;
		                else
		                	return 0;
					});

			    	if(sensors.length > 0){
						connect_chosen = sensors[0];
						let sensors_final = [];
						const high_rank = connect_chosen.array[0].rank;

						connect_chosen.array.forEach(function (obj) {
				            if(obj.rank == high_rank)
				            	sensors_final.push(obj);
				        });

				        if(sensors_final.length > 1){
				        	const position = randomIntFromInterval(0, (sensors_final.length-1));
				        	sensor_chosen = sensors_final[position];
			       		}else
			        		sensor_chosen = sensors_final[0];
			    	}

			    	try{
						assert.exists(sensor_chosen, 'Variável Existe!');
					}catch(err){
						console.log(err.message);
					}

					try{
						assert.exists(connect_chosen, 'Variável Existe!');
					}catch(err){
						console.log(err.message);
					}

					resolve({ status: 201, sensor: sensor_chosen, connect: connect_chosen });
				}else
					resolve({ status: 201, sensor: null, connect: null });
				
		    }
		    
		});

	});

/**
 * @return Retorna os serviços com analytics do algoritmo de matchmaking.
 */
exports.getSensorAlgorithmAnalytics = (lat, lng, category) => 
	
	new Promise((resolve,reject) => {

		let sensors =[];
		let chosen_group;

		const cypher = "MATCH (you:Profile) "
					+"MATCH (cn:Conection)-[:IS_NEAR]->(s:Sensor)-[:BELONGS_TO]->(c:Category {title: {category}}) "
					+"MATCH (a:Analytics)-[:BELONGS_TO]->(c:Category {title: {category}}) "
					+"MATCH (s)-[sr:IS_IN]->(g:Group)  "
					+"MATCH (cn)-[cnr:IS_IN]->(g2:Group) "
					+"MATCH (a)-[ar:IS_IN]->(g3:Group) "
					+"WHERE (sr.price + cnr.price + ar.price) <= you.budget "
					+"RETURN cn, cnr, s, sr, a ,ar, g.title, g2.title, g3.title ORDER BY cn.title, s.title, a.title";

		try{
			assert.isDefined(lat, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}
		
		try{
			assert.isDefined(lng, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}

		try{
			assert.isDefined(category, 'Variável Existe!');
		}catch(err){
			console.log(err.message);
		}			

		db.cypher({
		    query: cypher,
		    params: {
	            category: category											
		    },
		    lean: true
		}, (err, results) =>{

			try{
				assert.notExists(err, 'Sem erro!');
			}catch(err){
				console.log(err.message);
			}
			
			if (err) 
		    	reject({ status: 500, message: 'Internal Server Error !' });
		    else{
		    	let i, j;

		    	try{
					assert.isDefined(results, 'Vetor Existe!');
				}catch(err){
					console.log(err.message);
				}

		    	if(results && results.length > 0){

			    	results.forEach(function (obj) {
			            let cn = obj['cn'];
			            let cnr = obj['cnr'];
			            let s = obj['s'];
			            let sr = obj['sr'];
			            let a = obj['a'];
			            let ar = obj['ar'];

			            cn.rank = parseFloat(cnr.sum)/parseFloat(cnr.qty);
			            cn.price = cnr.price;
			            cn.category = obj['g2.title'];
			            s.rank = parseFloat(sr.sum)/parseFloat(sr.qty);
			            s.price = sr.price;
			            s.category = obj['g.title'];
			            a.rank = parseFloat(ar.sum)/parseFloat(ar.qty);
			            a.price = ar.price;
			            a.category = obj['g3.title'];

			            cn.sensor = s;
			            cn.analytics = a;

			            if(getDistanceFromLatLonInKm(lat, lng, cn.lat, cn.lng) < 1.5)
			            	sensors.push(cn);
			            
			        });

			    	sensors.sort(function(a,b) {  
					    if (a.rank < b.rank)
		                    return 1;
		                else if (a.rank > b.rank)
		                    return -1;
		                else if (a.sgnl_net < b.sgnl_net)
		                    return 1;
		                else if (a.sgnl_net > b.sgnl_net)
		                    return -1;
		                else if (a.batery < b.batery)
		                    return 1;
		                else if (a.batery > b.batery)
		                	return -1;
		                else if (a.price < b.price)
		                    return -1;
		                else if (a.price > b.price)
		                	return 1;

		                else if (a.sensor.rank < b.sensor.rank)
		                    return 1;
		                else if (a.sensor.rank > b.sensor.rank)
		                	return -1;
		                else if (a.sensor.price < b.sensor.price)
		                    return -1;
		                else if (a.sensor.price > b.sensor.price)
		                	return 1;

		                else if (a.analytics.rank < b.analytics.rank)
		                    return 1;
		                else if (a.analytics.rank > b.analytics.rank)
		                	return -1;
		                else if (a.analytics.price < b.analytics.price)
		                    return -1;
		                else if (a.analytics.price > b.analytics.price)
		                	return 1;
		                else
		                	return 0;
					});

					chosen_group = sensors[0];

					try{
						assert.exists(chosen_group.sensor, 'Variável Existe!');
					}catch(err){
						console.log(err.message);
					}

					try{
						assert.exists(chosen_group, 'Variável Existe!');
					}catch(err){
						console.log(err.message);
					}

					try{
						assert.exists(chosen_group.analytics, 'Variável Existe!');
					}catch(err){
						console.log(err.message);
					}


					resolve({ status: 201, sensor: chosen_group.sensor, connect: chosen_group, analytics : chosen_group.analytics });
				}else
					resolve({ status: 201, sensor: null, connect: null, analytics : null });
				
		    }
		    
		});

	});
```